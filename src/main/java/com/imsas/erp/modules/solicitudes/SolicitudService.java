package com.imsas.erp.modules.solicitudes;

import com.imsas.erp.modules.artes.Arte;
import com.imsas.erp.modules.artes.ArteRepository;
import com.imsas.erp.modules.contactos.Contacto;
import com.imsas.erp.modules.contactos.ContactoRepository;
import com.imsas.erp.modules.empresas.Empresa;
import com.imsas.erp.modules.empresas.EmpresaRepository;
import com.imsas.erp.modules.marcas.Marca;
import com.imsas.erp.modules.marcas.MarcaRepository;
import com.imsas.erp.modules.productos.Producto;
import com.imsas.erp.modules.productos.ProductoRepository;
import com.imsas.erp.modules.usuarios.Usuario;
import com.imsas.erp.shared.enums.EstadoSolicitud;
import com.imsas.erp.shared.enums.Rol;
import com.imsas.erp.shared.enums.TipoSolicitud;
import com.imsas.erp.shared.exceptions.BusinessException;
import com.imsas.erp.shared.exceptions.EntityNotFoundException;
import com.imsas.erp.shared.utils.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final ArteRepository      arteRepository;
    private final EmpresaRepository   empresaRepository;
    private final ContactoRepository  contactoRepository;
    private final MarcaRepository     marcaRepository;
    private final ProductoRepository  productoRepository;

    // ─── Listar ───────────────────────────────────────────────────────────────

    /**
     * Devuelve solicitudes activas con filtros opcionales.
     * SALES_REP solo ve sus propias solicitudes; los demás pueden filtrar por asesor.
     */
    @Transactional(readOnly = true)
    public Page<SolicitudResponse> listar(
            UUID empresaId,
            UUID asesorId,
            EstadoSolicitud estado,
            TipoSolicitud tipo,
            Pageable pageable,
            Usuario autenticado
    ) {
        UUID asesorFiltro = autenticado.getRol() == Rol.SALES_REP
                ? autenticado.getId()
                : asesorId;

        log.debug("Listando solicitudes — empresaId={} asesorId={} estado={} tipo={}",
                empresaId, asesorFiltro, estado, tipo);

        return solicitudRepository
                .buscarConFiltros(empresaId, asesorFiltro, estado, tipo, pageable)
                .map(SolicitudResponse::from);
    }

    // ─── Buscar por ID ────────────────────────────────────────────────────────

    /**
     * Devuelve el detalle de una solicitud activa. OPERATOR y todos los roles pueden leer.
     */
    @Transactional(readOnly = true)
    public SolicitudResponse buscarPorId(UUID id, Usuario autenticado) {
        Solicitud solicitud = findActivaOrThrow(id);
        verificarLectura(solicitud, autenticado);
        log.debug("Solicitud consultada: {} [{}]", solicitud.getCodigo(), id);
        return SolicitudResponse.from(solicitud);
    }

    // ─── Crear ────────────────────────────────────────────────────────────────

    /**
     * Crea una solicitud en estado BORRADOR.
     * Si es tipo DISEÑO con marca+producto, busca o crea el Arte correspondiente (RN-19).
     * Si es otro tipo con marca+producto, referencia el Arte vigente si existe (RN-21).
     */
    @Transactional
    public SolicitudResponse crear(SolicitudRequest request, Usuario autenticado) {
        Empresa empresa = findEmpresaActivaOrThrow(request.empresaId());
        verificarAccesoCartera(empresa, autenticado, "crear solicitudes para");

        Contacto contacto = request.contactoId()       != null ? findContactoActivoOrThrow(request.contactoId())   : null;
        Marca    marca    = request.marcaId()           != null ? findMarcaActivaOrThrow(request.marcaId())          : null;
        Producto producto = request.productoId()        != null ? findProductoActivoOrThrow(request.productoId())    : null;
        Solicitud origen  = request.solicitudOrigenId() != null ? findActivaOrThrow(request.solicitudOrigenId())    : null;

        Arte arte = resolverArte(request.tipo(), empresa, marca, producto);

        Short cantVersiones = validarCantidadVersiones(request.cantidadVersionesEntregadas(), request.tipo());

        Solicitud solicitud = Solicitud.builder()
                .asesor(autenticado)
                .empresa(empresa)
                .contacto(contacto)
                .marca(marca)
                .producto(producto)
                .detalleProducto(request.detalleProducto())
                .solicitudOrigen(origen)
                .tipo(request.tipo())
                .descripcion(request.descripcion())
                .cantidad(request.cantidad())
                .tecnica(request.tecnica())
                .ancho(request.ancho())
                .largo(request.largo())
                .coloresFrente(request.coloresFrente())
                .coloresReverso(request.coloresReverso())
                .costura(request.costura())
                .ordenCompra(request.ordenCompra())
                .precioReferencia(request.precioReferencia())
                .abono(request.abono() != null ? request.abono() : BigDecimal.ZERO)
                .observaciones(request.observaciones())
                .arte(arte)
                .cantidadVersionesEntregadas(cantVersiones)
                .build();

        Solicitud guardada = solicitudRepository.save(solicitud);
        log.info("Solicitud creada [{}] tipo={} empresa='{}' por {}",
                guardada.getId(), guardada.getTipo(),
                empresa.getRazonSocial(), autenticado.getEmail());
        return SolicitudResponse.from(guardada);
    }

    // ─── Actualizar ───────────────────────────────────────────────────────────

    /**
     * Actualiza una solicitud. Solo permitido en estado BORRADOR.
     * Recalcula la vinculación de Arte si cambia empresa/marca/producto.
     */
    @Transactional
    public SolicitudResponse actualizar(UUID id, SolicitudRequest request, Usuario autenticado) {
        Solicitud solicitud = findActivaOrThrow(id);

        if (solicitud.getEstado() != EstadoSolicitud.BORRADOR) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "ESTADO_INVALIDO",
                    "Solo se pueden editar solicitudes en estado BORRADOR");
        }
        verificarEsSuSolicitud(solicitud, autenticado);

        Empresa empresa = findEmpresaActivaOrThrow(request.empresaId());
        verificarAccesoCartera(empresa, autenticado, "modificar solicitudes de");

        Contacto contacto = request.contactoId()       != null ? findContactoActivoOrThrow(request.contactoId())   : null;
        Marca    marca    = request.marcaId()           != null ? findMarcaActivaOrThrow(request.marcaId())          : null;
        Producto producto = request.productoId()        != null ? findProductoActivoOrThrow(request.productoId())    : null;
        Solicitud origen  = request.solicitudOrigenId() != null ? findActivaOrThrow(request.solicitudOrigenId())    : null;

        // Recalcular Arte si la combinación empresa/marca/producto cambió
        Arte arte = recalcularArteEnActualizacion(solicitud, request.tipo(), empresa, marca, producto);

        Short cantVersiones = validarCantidadVersiones(request.cantidadVersionesEntregadas(), request.tipo());

        solicitud.setEmpresa(empresa);
        solicitud.setContacto(contacto);
        solicitud.setMarca(marca);
        solicitud.setProducto(producto);
        solicitud.setDetalleProducto(request.detalleProducto());
        solicitud.setSolicitudOrigen(origen);
        solicitud.setTipo(request.tipo());
        solicitud.setDescripcion(request.descripcion());
        solicitud.setCantidad(request.cantidad());
        solicitud.setTecnica(request.tecnica());
        solicitud.setAncho(request.ancho());
        solicitud.setLargo(request.largo());
        solicitud.setColoresFrente(request.coloresFrente());
        solicitud.setColoresReverso(request.coloresReverso());
        solicitud.setCostura(request.costura());
        solicitud.setOrdenCompra(request.ordenCompra());
        solicitud.setPrecioReferencia(request.precioReferencia());
        solicitud.setAbono(request.abono() != null ? request.abono() : BigDecimal.ZERO);
        solicitud.setObservaciones(request.observaciones());
        solicitud.setArte(arte);
        solicitud.setCantidadVersionesEntregadas(cantVersiones);

        Solicitud guardada = solicitudRepository.save(solicitud);
        log.info("Solicitud actualizada [{}] por {}", id, autenticado.getEmail());
        return SolicitudResponse.from(guardada);
    }

    // ─── Soft delete ──────────────────────────────────────────────────────────

    /**
     * Desactiva lógicamente una solicitud (soft delete). Solo ADMIN/SUPERADMIN.
     */
    @Transactional
    public void desactivar(UUID id, Usuario autenticado) {
        Solicitud solicitud = findActivaOrThrow(id);
        solicitud.setActivo(false);
        solicitudRepository.save(solicitud);
        log.info("Solicitud desactivada [{}] por {}", id, autenticado.getEmail());
    }

    // ─── Cambiar Estado ───────────────────────────────────────────────────────

    /**
     * Aplica una transición de estado según la máquina de estados (SDD §6.3).
     * <p>
     * Transiciones válidas:
     * <pre>
     * BORRADOR   → CONFIRMAR  (SALES_REP propio, ADMIN, SUPERADMIN) — genera código P- y BD-
     * CONFIRMAR  → PENDIENTE  (MANAGER, ADMIN, SUPERADMIN)
     * PENDIENTE  → COMPLETADO (MANAGER, ADMIN, SUPERADMIN) — incrementa versión Arte si DISEÑO
     * *          → CANCELADO  (excepto COMPLETADO) — requiere observacion_cancelacion (RN-13)
     * </pre>
     */
    @Transactional
    public SolicitudResponse cambiarEstado(UUID id, CambiarEstadoRequest request, Usuario autenticado) {
        Solicitud solicitud = findActivaOrThrow(id);
        EstadoSolicitud estadoActual = solicitud.getEstado();
        EstadoSolicitud nuevoEstado  = request.nuevoEstado();

        validarTransicion(estadoActual, nuevoEstado);
        validarPermisosTransicion(solicitud, nuevoEstado, autenticado);

        if (nuevoEstado == EstadoSolicitud.CANCELADO) {
            if (request.observacionCancelacion() == null || request.observacionCancelacion().isBlank()) {
                throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "OBSERVACION_REQUERIDA",
                        "La observación de cancelación es obligatoria (RN-13)");
            }
            solicitud.setObservacionCancelacion(request.observacionCancelacion().trim());
        }

        // BORRADOR → CONFIRMAR: asignar código de solicitud y código BD (RN-04, RN-07)
        if (estadoActual == EstadoSolicitud.BORRADOR && nuevoEstado == EstadoSolicitud.CONFIRMAR) {
            asignarCodigos(solicitud);
        }

        // PENDIENTE → COMPLETADO: si es DISEÑO, actualizar versión del Arte (RN-18, RN-20)
        if (estadoActual == EstadoSolicitud.PENDIENTE && nuevoEstado == EstadoSolicitud.COMPLETADO) {
            if (solicitud.getTipo() == TipoSolicitud.DISEÑO && solicitud.getArte() != null) {
                completarVersionArte(solicitud);
            }
        }

        solicitud.setEstado(nuevoEstado);
        Solicitud guardada = solicitudRepository.save(solicitud);
        log.info("Solicitud [{}] {} → {} por {}",
                id, estadoActual, nuevoEstado, autenticado.getEmail());
        return SolicitudResponse.from(guardada);
    }

    // ─── Versionar BD ─────────────────────────────────────────────────────────

    /**
     * Incrementa la versión del código Base de Datos del cliente (RN-07, RN-08).
     * Requiere que la solicitud ya tenga código asignado y motivo obligatorio.
     * Roles permitidos: SALES_REP (propio), ADMIN, SUPERADMIN (RN-09).
     */
    @Transactional
    public SolicitudResponse versionarBd(UUID id, VersionarBdRequest request, Usuario autenticado) {
        Solicitud solicitud = findActivaOrThrow(id);

        if (solicitud.getCodigo() == null) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "CODIGO_NO_ASIGNADO",
                    "La solicitud aún no tiene código; debe confirmarla primero");
        }

        Rol rol = autenticado.getRol();
        if (rol != Rol.SALES_REP && rol != Rol.ADMIN && rol != Rol.SUPERADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                    "Solo SALES_REP, ADMIN y SUPERADMIN pueden versionar el código BD (RN-09)");
        }
        if (rol == Rol.SALES_REP) {
            verificarEsSuSolicitud(solicitud, autenticado);
        }

        short nuevaVersion = (short) (solicitud.getVersionBd() + 1);
        String baseBd    = CodeGenerator.deriveCodigoBaseDatos(solicitud.getCodigo());
        String nuevoCodigo = CodeGenerator.buildVersionedCodigoBd(baseBd, nuevaVersion);

        solicitud.setVersionBd(nuevaVersion);
        solicitud.setCodigoBaseDatos(nuevoCodigo);
        solicitud.setMotivoVersionBd(request.motivo().trim());

        Solicitud guardada = solicitudRepository.save(solicitud);
        log.info("BD versionada: solicitud [{}] → {} por {}",
                id, nuevoCodigo, autenticado.getEmail());
        return SolicitudResponse.from(guardada);
    }

    // ─── Convertir ────────────────────────────────────────────────────────────

    /**
     * Crea una nueva solicitud tipo PEDIDO o REPOSICION copiando los campos de la
     * solicitud de origen (que debe ser DISEÑO o PEDIDO con código asignado).
     * Vincula el mismo Arte de la solicitud de origen (RN-21).
     * La nueva solicitud queda en BORRADOR y referencia la solicitud origen (RN-15).
     */
    @Transactional
    public SolicitudResponse convertir(UUID id, ConvertirRequest request, Usuario autenticado) {
        Solicitud origen = findActivaOrThrow(id);

        if (origen.getTipo() != TipoSolicitud.DISEÑO && origen.getTipo() != TipoSolicitud.PEDIDO) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "TIPO_ORIGEN_INVALIDO",
                    "Solo solicitudes de tipo DISEÑO o PEDIDO pueden convertirse en otra solicitud");
        }
        if (request.tipo() != TipoSolicitud.PEDIDO && request.tipo() != TipoSolicitud.REPOSICION) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "TIPO_DESTINO_INVALIDO",
                    "Solo se puede convertir a PEDIDO o REPOSICION");
        }
        if (origen.getCodigo() == null) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "CODIGO_NO_ASIGNADO",
                    "La solicitud de origen debe tener código asignado antes de convertirse");
        }
        if (origen.getEstado() == EstadoSolicitud.CANCELADO) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "ORIGEN_CANCELADO",
                    "No se puede convertir una solicitud cancelada");
        }

        Rol rol = autenticado.getRol();
        if (rol != Rol.SALES_REP && rol != Rol.ADMIN && rol != Rol.SUPERADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                    "No tienes permisos para convertir solicitudes");
        }
        if (rol == Rol.SALES_REP) {
            verificarEsSuSolicitud(origen, autenticado);
        }

        Solicitud nueva = Solicitud.builder()
                .asesor(autenticado)
                .empresa(origen.getEmpresa())
                .contacto(origen.getContacto())
                .marca(origen.getMarca())
                .producto(origen.getProducto())
                .detalleProducto(origen.getDetalleProducto())
                .solicitudOrigen(origen)
                .tipo(request.tipo())
                .descripcion(origen.getDescripcion())
                .cantidad(request.cantidad()    != null ? request.cantidad()    : origen.getCantidad())
                .tecnica(origen.getTecnica())
                .ancho(request.ancho()          != null ? request.ancho()       : origen.getAncho())
                .largo(request.largo()          != null ? request.largo()       : origen.getLargo())
                .coloresFrente(origen.getColoresFrente())
                .coloresReverso(origen.getColoresReverso())
                .costura(origen.getCostura())
                .ordenCompra(request.ordenCompra() != null ? request.ordenCompra() : origen.getOrdenCompra())
                .precioReferencia(origen.getPrecioReferencia())
                .abono(BigDecimal.ZERO)
                .observaciones(request.observaciones() != null ? request.observaciones() : origen.getObservaciones())
                .arte(origen.getArte())
                .build();

        Solicitud guardada = solicitudRepository.save(nueva);
        log.info("Solicitud [{}] {} convertida a {} [{}] por {}",
                id, origen.getTipo(), request.tipo(), guardada.getId(), autenticado.getEmail());
        return SolicitudResponse.from(guardada);
    }

    // ─── Lógica de Arte ───────────────────────────────────────────────────────

    /**
     * Para tipo DISEÑO: busca Arte vigente por empresa+marca+producto; si no existe, crea uno nuevo.
     * Para otros tipos: solo referencia el Arte vigente si existe; no lo crea (RN-21).
     */
    private Arte resolverArte(TipoSolicitud tipo, Empresa empresa, Marca marca, Producto producto) {
        if (marca == null || producto == null) return null;

        if (tipo == TipoSolicitud.DISEÑO) {
            return arteRepository
                    .findByEmpresaIdAndMarcaIdAndProductoIdAndActivoTrue(
                            empresa.getId(), marca.getId(), producto.getId())
                    .orElseGet(() -> crearNuevoArte(empresa, marca, producto));
        }

        return arteRepository
                .findByEmpresaIdAndMarcaIdAndProductoIdAndActivoTrue(
                        empresa.getId(), marca.getId(), producto.getId())
                .orElse(null);
    }

    /**
     * Crea y persiste un Arte nuevo con el siguiente código consecutivo (RN-18).
     */
    private Arte crearNuevoArte(Empresa empresa, Marca marca, Producto producto) {
        long consecutivo = arteRepository.nextConsecutivoArte();
        String codigoBase = CodeGenerator.generateCodigoArte(consecutivo);

        Arte arte = Arte.builder()
                .codigo(codigoBase)
                .empresa(empresa)
                .marca(marca)
                .producto(producto)
                .build();

        Arte guardado = arteRepository.save(arte);
        log.info("Arte creado: {} para empresa='{}' marca='{}' producto='{}'",
                guardado.getCodigo(),
                empresa.getRazonSocial(),
                marca.getNombre(),
                producto.getNombre());
        return guardado;
    }

    /**
     * Al completar una solicitud DISEÑO, incrementa la versión del Arte vinculado (RN-20).
     * Si la nueva versión supera 99, desactiva el Arte actual y crea uno nuevo (RN-18).
     */
    private void completarVersionArte(Solicitud solicitud) {
        Arte arte = solicitud.getArte();
        short incremento = solicitud.getCantidadVersionesEntregadas() != null
                ? solicitud.getCantidadVersionesEntregadas()
                : 1;

        int nuevaVersion = arte.getVersionActual() + incremento;

        if (nuevaVersion > 99) {
            // Arte llegó al límite: desactivar y crear uno nuevo con versión 0
            arte.setActivo(false);
            arteRepository.save(arte);
            Arte nuevoArte = crearNuevoArte(arte.getEmpresa(), arte.getMarca(), arte.getProducto());
            solicitud.setVersionArteGenerada((short) 0);
            log.info("Arte {} superó versión 99; nuevo Arte {} creado",
                    arte.getCodigo(), nuevoArte.getCodigo());
        } else {
            arte.setVersionActual((short) nuevaVersion);
            arteRepository.save(arte);
            solicitud.setVersionArteGenerada((short) nuevaVersion);
            log.info("Arte {} actualizado a versión {}", arte.getCodigo(), nuevaVersion);
        }
    }

    /**
     * Recalcula la vinculación del Arte al actualizar una solicitud BORRADOR.
     * Solo reinvoca la lógica de resolución si la combinación empresa/marca/producto cambió.
     */
    private Arte recalcularArteEnActualizacion(
            Solicitud solicitud, TipoSolicitud nuevoTipo,
            Empresa empresa, Marca marca, Producto producto
    ) {
        boolean combinacionCambio =
                !empresa.getId().equals(solicitud.getEmpresa().getId())
                || !Objects.equals(
                        marca    != null ? marca.getId()    : null,
                        solicitud.getMarca()    != null ? solicitud.getMarca().getId()    : null)
                || !Objects.equals(
                        producto != null ? producto.getId() : null,
                        solicitud.getProducto() != null ? solicitud.getProducto().getId() : null);

        if (combinacionCambio || nuevoTipo != solicitud.getTipo()) {
            return resolverArte(nuevoTipo, empresa, marca, producto);
        }
        return solicitud.getArte();
    }

    // ─── Generación de códigos ────────────────────────────────────────────────

    /**
     * Asigna el código P- y el código BD- inicial al pasar de BORRADOR a CONFIRMAR (RN-04, RN-07).
     */
    private void asignarCodigos(Solicitud solicitud) {
        long consecutivo = solicitudRepository.nextConsecutivo();
        String codigo    = CodeGenerator.generate(consecutivo);
        String codigoBd  = CodeGenerator.initialVersionedCodigoBd(codigo);
        solicitud.setCodigo(codigo);
        solicitud.setCodigoBaseDatos(codigoBd);
        log.info("Códigos asignados: {} / {} a solicitud [{}]",
                codigo, codigoBd, solicitud.getId());
    }

    // ─── Máquina de estados ───────────────────────────────────────────────────

    private void validarTransicion(EstadoSolicitud desde, EstadoSolicitud hasta) {
        boolean valida = switch (desde) {
            case BORRADOR   -> hasta == EstadoSolicitud.CONFIRMAR || hasta == EstadoSolicitud.CANCELADO;
            case CONFIRMAR  -> hasta == EstadoSolicitud.PENDIENTE  || hasta == EstadoSolicitud.CANCELADO;
            case PENDIENTE  -> hasta == EstadoSolicitud.COMPLETADO || hasta == EstadoSolicitud.CANCELADO;
            case COMPLETADO -> false;
            case CANCELADO  -> false;
        };

        if (!valida) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "TRANSICION_INVALIDA",
                    "No se puede transicionar de " + desde + " a " + hasta);
        }
    }

    private void validarPermisosTransicion(Solicitud solicitud, EstadoSolicitud nuevoEstado, Usuario autenticado) {
        Rol rol = autenticado.getRol();

        if (rol == Rol.SUPERADMIN || rol == Rol.ADMIN) return;

        EstadoSolicitud estadoActual = solicitud.getEstado();

        if (rol == Rol.MANAGER) {
            boolean permitido =
                    (estadoActual == EstadoSolicitud.CONFIRMAR && nuevoEstado == EstadoSolicitud.PENDIENTE)
                    || (estadoActual == EstadoSolicitud.PENDIENTE  && nuevoEstado == EstadoSolicitud.COMPLETADO)
                    || (nuevoEstado  == EstadoSolicitud.CANCELADO  && estadoActual != EstadoSolicitud.BORRADOR);
            if (!permitido) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                        "No tienes permisos para esta transición de estado");
            }
            return;
        }

        if (rol == Rol.SALES_REP) {
            verificarEsSuSolicitud(solicitud, autenticado);
            boolean permitido =
                    (estadoActual == EstadoSolicitud.BORRADOR && nuevoEstado == EstadoSolicitud.CONFIRMAR)
                    || (estadoActual == EstadoSolicitud.BORRADOR && nuevoEstado == EstadoSolicitud.CANCELADO);
            if (!permitido) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                        "SALES_REP solo puede confirmar o cancelar solicitudes propias en BORRADOR");
            }
            return;
        }

        // OPERATOR: sin acceso a transiciones de estado
        throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                "No tienes permisos para cambiar el estado de solicitudes");
    }

    // ─── Validaciones auxiliares ──────────────────────────────────────────────

    private Short validarCantidadVersiones(Short cantidad, TipoSolicitud tipo) {
        if (cantidad == null) return null;
        if (tipo != TipoSolicitud.DISEÑO) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "CAMPO_NO_APLICA",
                    "cantidadVersionesEntregadas solo aplica para solicitudes tipo DISEÑO (RN-20)");
        }
        if (cantidad < 1 || cantidad > 3) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "VERSIONES_INVALIDAS",
                    "La cantidad de versiones entregadas debe ser entre 1 y 3 (RN-20)");
        }
        return cantidad;
    }

    /** Verifica que SALES_REP acceda solo a solicitudes propias. */
    private void verificarEsSuSolicitud(Solicitud solicitud, Usuario autenticado) {
        if (autenticado.getRol() != Rol.SALES_REP) return;
        if (!solicitud.getAsesor().getId().equals(autenticado.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                    "No tienes permisos sobre esta solicitud");
        }
    }

    /** Verifica acceso de lectura: SALES_REP solo ve sus propias solicitudes. */
    private void verificarLectura(Solicitud solicitud, Usuario autenticado) {
        if (autenticado.getRol() == Rol.SALES_REP) {
            verificarEsSuSolicitud(solicitud, autenticado);
        }
    }

    /** Verifica que SALES_REP acceda solo a empresas de su cartera (creadoPor). */
    private void verificarAccesoCartera(Empresa empresa, Usuario autenticado, String accion) {
        if (autenticado.getRol() != Rol.SALES_REP) return;
        boolean esDesuCartera = empresa.getCreadoPor() != null
                && empresa.getCreadoPor().getId().equals(autenticado.getId());
        if (!esDesuCartera) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                    "No tienes permisos para " + accion + " esta empresa");
        }
    }

    // ─── Lookups ──────────────────────────────────────────────────────────────

    private Solicitud findActivaOrThrow(UUID id) {
        return solicitudRepository.findById(id)
                .filter(com.imsas.erp.shared.entity.BaseEntity::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud", id));
    }

    private Empresa findEmpresaActivaOrThrow(UUID id) {
        return empresaRepository.findById(id)
                .filter(Empresa::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Empresa", id));
    }

    private Contacto findContactoActivoOrThrow(UUID id) {
        return contactoRepository.findById(id)
                .filter(Contacto::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Contacto", id));
    }

    private Marca findMarcaActivaOrThrow(UUID id) {
        return marcaRepository.findById(id)
                .filter(Marca::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Marca", id));
    }

    private Producto findProductoActivoOrThrow(UUID id) {
        return productoRepository.findById(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Producto", id));
    }
}
