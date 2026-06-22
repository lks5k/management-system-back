package com.imsas.erp.modules.productos;

import com.imsas.erp.modules.usuarios.Usuario;
import com.imsas.erp.shared.exceptions.BusinessException;
import com.imsas.erp.shared.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio de gestión del catálogo de productos fabricables por IMSAS.
 *
 * <h3>Modelo de permisos</h3>
 * <ul>
 *   <li><b>SUPERADMIN / ADMIN</b>: CRUD completo sobre cualquier producto.</li>
 *   <li><b>MANAGER / SALES_REP</b>: solo lectura — consultan el catálogo para
 *       seleccionar producto en sus solicitudes.</li>
 *   <li><b>OPERATOR</b>: sin acceso a este módulo (RN-24). Bloqueado en el controller.</li>
 * </ul>
 *
 * <h3>Reglas críticas implementadas</h3>
 * <ul>
 *   <li>RN-25: {@code nombre} siempre se persiste en mayúsculas y sin espacios extremos.</li>
 *   <li>Unicidad global: no pueden existir dos productos con el mismo nombre normalizado.</li>
 *   <li>RN-12: soft delete; nunca DELETE físico.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    // ─── Consulta ────────────────────────────────────────────────────────────

    /**
     * Lista todos los productos activos con paginación.
     *
     * <p>Accesible para todos los roles excepto OPERATOR (bloqueado en controller).
     *
     * @param pageable configuración de página y ordenamiento
     * @return página de {@link ProductoResponse}
     */
    @Transactional(readOnly = true)
    public Page<ProductoResponse> listar(Pageable pageable) {
        return productoRepository.findAllByActivoTrue(pageable)
                .map(ProductoResponse::from);
    }

    /**
     * Busca un producto activo por ID.
     *
     * @param id UUID del producto
     * @return DTO del producto encontrado
     * @throws EntityNotFoundException si no existe producto activo con ese ID
     */
    @Transactional(readOnly = true)
    public ProductoResponse buscarPorId(UUID id) {
        return ProductoResponse.from(findActivoOrThrow(id));
    }

    // ─── Creación ─────────────────────────────────────────────────────────────

    /**
     * Registra un nuevo producto en el catálogo.
     *
     * <p>El nombre se normaliza (trim + uppercase) antes de verificar unicidad
     * y antes de persistir (RN-25). Si ya existe un producto con el mismo nombre
     * normalizado, se lanza 409.
     *
     * @param request     datos del producto validados por JSR-380
     * @param autenticado usuario que realiza la operación (solo ADMIN/SUPERADMIN llegan aquí)
     * @return DTO del producto creado
     * @throws BusinessException 409 si el nombre ya existe en el catálogo
     */
    @Transactional
    public ProductoResponse crear(ProductoRequest request, Usuario autenticado) {
        String nombreNorm = normalizar(request.nombre());
        verificarNombreUnico(nombreNorm, null);

        Producto producto = Producto.builder()
                .nombre(nombreNorm)
                .build();

        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado: '{}' [{}] por {}",
                guardado.getNombre(), guardado.getId(), autenticado.getEmail());

        return ProductoResponse.from(guardado);
    }

    // ─── Actualización ────────────────────────────────────────────────────────

    /**
     * Actualiza el nombre de un producto activo.
     *
     * <p>El nombre se normaliza antes de verificar unicidad y antes de persistir.
     *
     * @param id          UUID del producto a actualizar
     * @param request     datos actualizados validados por JSR-380
     * @param autenticado usuario que realiza la operación
     * @return DTO actualizado
     * @throws EntityNotFoundException si no existe producto activo con ese ID
     * @throws BusinessException       409 si el nuevo nombre ya existe en el catálogo
     */
    @Transactional
    public ProductoResponse actualizar(UUID id, ProductoRequest request, Usuario autenticado) {
        Producto producto = findActivoOrThrow(id);

        String nombreNorm = normalizar(request.nombre());
        verificarNombreUnico(nombreNorm, id);

        producto.setNombre(nombreNorm);

        Producto guardado = productoRepository.save(producto);
        log.info("Producto actualizado: '{}' [{}] por {}",
                guardado.getNombre(), guardado.getId(), autenticado.getEmail());

        return ProductoResponse.from(guardado);
    }

    // ─── Soft delete ──────────────────────────────────────────────────────────

    /**
     * Desactiva un producto (soft delete). Solo ADMIN y SUPERADMIN pueden ejecutar
     * esta operación; el controller lo refuerza con {@code @PreAuthorize}.
     *
     * @param id          UUID del producto a desactivar
     * @param autenticado usuario que realiza la operación
     * @throws EntityNotFoundException si no existe producto activo con ese ID
     */
    @Transactional
    public void desactivar(UUID id, Usuario autenticado) {
        Producto producto = findActivoOrThrow(id);
        producto.setActivo(false);
        productoRepository.save(producto);
        log.info("Producto desactivado: '{}' [{}] por {}",
                producto.getNombre(), producto.getId(), autenticado.getEmail());
    }

    // ─── Métodos auxiliares privados ──────────────────────────────────────────

    /**
     * Busca un producto activo por ID o lanza {@link EntityNotFoundException}.
     *
     * @param id UUID del producto
     * @return entidad encontrada
     * @throws EntityNotFoundException si no existe producto activo con ese ID
     */
    private Producto findActivoOrThrow(UUID id) {
        return productoRepository.findById(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Producto", id));
    }

    /**
     * Verifica que el nombre normalizado no esté en uso por otro producto activo (RN-25).
     *
     * @param nombreNorm nombre ya normalizado (trim + uppercase)
     * @param excluirId  ID del producto a excluir en actualizaciones; {@code null} en creación
     * @throws BusinessException 409 si el nombre ya existe en el catálogo
     */
    private void verificarNombreUnico(String nombreNorm, UUID excluirId) {
        boolean existe = excluirId == null
                ? productoRepository.existsByNombre(nombreNorm)
                : productoRepository.existsByNombreAndIdNot(nombreNorm, excluirId);

        if (existe) {
            throw new BusinessException(HttpStatus.CONFLICT, "DUPLICATE_PRODUCTO",
                    "Ya existe un producto con el nombre '" + nombreNorm + "'");
        }
    }

    /**
     * Normaliza un nombre de producto: elimina espacios extremos y convierte a mayúsculas (RN-25).
     *
     * @param nombre nombre a normalizar
     * @return nombre normalizado
     */
    private String normalizar(String nombre) {
        return nombre.trim().toUpperCase();
    }
}
