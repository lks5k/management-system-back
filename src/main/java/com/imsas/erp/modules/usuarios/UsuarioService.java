package com.imsas.erp.modules.usuarios;

import com.imsas.erp.shared.enums.Rol;
import com.imsas.erp.shared.exceptions.BusinessException;
import com.imsas.erp.shared.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio de gestión de usuarios.
 *
 * <h3>Modelo de permisos</h3>
 * <ul>
 *   <li><b>SUPERADMIN</b>: puede crear, editar, desactivar y ver cualquier usuario,
 *       incluyendo otros SUPERADMIN.</li>
 *   <li><b>ADMIN</b>: puede crear, editar, desactivar y ver cualquier usuario
 *       <em>excepto</em> los de rol SUPERADMIN.</li>
 *   <li><b>Cualquier rol</b>: puede ver su propio perfil y cambiar su propia contraseña.</li>
 * </ul>
 *
 * <h3>Reglas críticas implementadas</h3>
 * <ul>
 *   <li>Unicidad de email (activo e inactivo).</li>
 *   <li>Contraseña inicial obligatoria al crear.</li>
 *   <li>Solo SUPERADMIN puede asignar o reasignar el rol SUPERADMIN.</li>
 *   <li>Ningún usuario puede desactivar su propia cuenta.</li>
 *   <li>Cambio de contraseña propio requiere {@code passwordActual} correcto.</li>
 *   <li>Reset administrativo (ADMIN/SUPERADMIN sobre otro usuario) no requiere
 *       {@code passwordActual}; pero ADMIN no puede resetear contraseña de SUPERADMIN.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;

    // ─── Consulta ────────────────────────────────────────────────────────────

    /**
     * Lista usuarios activos con paginación.
     *
     * <p>SUPERADMIN ve todos; ADMIN ve todos excepto usuarios con rol SUPERADMIN.
     *
     * @param pageable    configuración de página y ordenamiento
     * @param autenticado usuario que realiza la consulta
     * @return página de {@link UsuarioResponse}
     */
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable, Usuario autenticado) {
        Page<Usuario> pagina = autenticado.getRol() == Rol.SUPERADMIN
                ? usuarioRepository.findAllByActivoTrue(pageable)
                : usuarioRepository.findAllByActivoTrueAndRolNot(Rol.SUPERADMIN, pageable);

        return pagina.map(UsuarioResponse::from);
    }

    /**
     * Busca un usuario activo por ID.
     *
     * <p>Cualquier usuario puede consultar su propio perfil.
     * ADMIN y SUPERADMIN pueden consultar cualquier perfil,
     * salvo que ADMIN no puede consultar perfiles SUPERADMIN.
     *
     * @param id          ID del usuario a consultar
     * @param autenticado usuario que realiza la consulta
     * @return DTO del usuario encontrado
     * @throws EntityNotFoundException si no existe usuario activo con ese ID
     * @throws BusinessException       403 si ADMIN intenta ver un SUPERADMIN
     *                                 o si un rol sin privilegios intenta ver a otro usuario
     */
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(UUID id, Usuario autenticado) {
        Usuario target = findActivoOrThrow(id);
        verificarAccesoSobreTarget(target, autenticado, "consultar");
        return UsuarioResponse.from(target);
    }

    // ─── Creación ─────────────────────────────────────────────────────────────

    /**
     * Crea un nuevo usuario.
     *
     * <p>Reglas aplicadas:
     * <ol>
     *   <li>Email único (activo e inactivo).</li>
     *   <li>{@code passwordInicial} obligatorio.</li>
     *   <li>Solo SUPERADMIN puede crear usuarios con rol SUPERADMIN.</li>
     * </ol>
     *
     * @param request     datos del nuevo usuario
     * @param autenticado usuario que realiza la operación
     * @return DTO del usuario creado
     * @throws BusinessException 409 si el email ya existe, 400 si falta contraseña inicial,
     *                           403 si ADMIN intenta crear SUPERADMIN
     */
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request, Usuario autenticado) {
        verificarPrivilegioRol(request.rol(), autenticado, "crear");
        verificarEmailUnico(request.email(), null);

        if (request.passwordInicial() == null || request.passwordInicial().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PASSWORD_REQUIRED",
                    "La contraseña inicial es obligatoria al crear un usuario");
        }

        Usuario nuevo = Usuario.builder()
                .nombre(request.nombre().trim())
                .email(request.email().trim().toLowerCase())
                .password(passwordEncoder.encode(request.passwordInicial()))
                .rol(request.rol())
                .build();

        Usuario guardado = usuarioRepository.save(nuevo);
        log.info("Usuario creado: {} [{}] por {}", guardado.getEmail(), guardado.getRol(),
                autenticado.getEmail());

        return UsuarioResponse.from(guardado);
    }

    // ─── Actualización ────────────────────────────────────────────────────────

    /**
     * Actualiza nombre, email y rol de un usuario activo.
     *
     * <p>La contraseña NO se modifica por este método; usar
     * {@link #cambiarPassword(UUID, CambioPasswordRequest, Usuario)}.
     *
     * <p>Reglas aplicadas:
     * <ol>
     *   <li>ADMIN no puede modificar usuarios SUPERADMIN.</li>
     *   <li>Email único excluyendo al propio usuario.</li>
     *   <li>Solo SUPERADMIN puede asignar el rol SUPERADMIN.</li>
     * </ol>
     *
     * @param id          ID del usuario a actualizar
     * @param request     datos actualizados
     * @param autenticado usuario que realiza la operación
     * @return DTO actualizado
     * @throws EntityNotFoundException si no existe usuario activo con ese ID
     * @throws BusinessException       403 si ADMIN intenta modificar SUPERADMIN o asignar ese rol;
     *                                 409 si el email ya pertenece a otro usuario
     */
    @Transactional
    public UsuarioResponse actualizar(UUID id, UsuarioRequest request, Usuario autenticado) {
        Usuario target = findActivoOrThrow(id);
        verificarAccesoSobreTarget(target, autenticado, "modificar");
        verificarPrivilegioRol(request.rol(), autenticado, "asignar");
        verificarEmailUnico(request.email(), id);

        target.setNombre(request.nombre().trim());
        target.setEmail(request.email().trim().toLowerCase());
        target.setRol(request.rol());

        Usuario guardado = usuarioRepository.save(target);
        log.info("Usuario actualizado: {} por {}", guardado.getEmail(), autenticado.getEmail());

        return UsuarioResponse.from(guardado);
    }

    // ─── Soft delete ──────────────────────────────────────────────────────────

    /**
     * Desactiva un usuario (soft delete — RN-12).
     *
     * <p>Reglas aplicadas:
     * <ol>
     *   <li>ADMIN no puede desactivar usuarios SUPERADMIN.</li>
     *   <li>Ningún usuario puede desactivarse a sí mismo.</li>
     * </ol>
     *
     * @param id          ID del usuario a desactivar
     * @param autenticado usuario que realiza la operación
     * @throws EntityNotFoundException si no existe usuario activo con ese ID
     * @throws BusinessException       403 en violación de permisos;
     *                                 400 si intenta autodesactivarse
     */
    @Transactional
    public void desactivar(UUID id, Usuario autenticado) {
        Usuario target = findActivoOrThrow(id);

        if (id.equals(autenticado.getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "SELF_DEACTIVATION",
                    "No puedes desactivar tu propia cuenta");
        }

        verificarAccesoSobreTarget(target, autenticado, "desactivar");

        target.setActivo(false);
        usuarioRepository.save(target);
        log.info("Usuario desactivado: {} por {}", target.getEmail(), autenticado.getEmail());
    }

    // ─── Cambio de contraseña ─────────────────────────────────────────────────

    /**
     * Cambia la contraseña de un usuario activo.
     *
     * <p>Semántica del campo {@code passwordActual}:
     * <ul>
     *   <li>Si el solicitante es el <b>propio usuario</b>: {@code passwordActual} es obligatorio
     *       y se verifica contra el hash almacenado.</li>
     *   <li>Si el solicitante es <b>ADMIN o SUPERADMIN</b> actuando sobre otro usuario:
     *       {@code passwordActual} se ignora (reset administrativo).</li>
     * </ul>
     *
     * @param id          ID del usuario cuya contraseña se cambia
     * @param request     DTO con contraseña actual (opcional) y nueva contraseña
     * @param autenticado usuario que realiza la operación
     * @throws EntityNotFoundException si no existe usuario activo con ese ID
     * @throws BusinessException       400 si falta {@code passwordActual} o es incorrecta;
     *                                 403 si el solicitante no tiene permiso
     */
    @Transactional
    public void cambiarPassword(UUID id, CambioPasswordRequest request, Usuario autenticado) {
        Usuario target = findActivoOrThrow(id);

        boolean esPropioUsuario = id.equals(autenticado.getId());
        boolean esAdmin = autenticado.getRol() == Rol.ADMIN
                || autenticado.getRol() == Rol.SUPERADMIN;

        if (!esPropioUsuario && !esAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                    "No tienes permisos para cambiar la contraseña de otro usuario");
        }

        // ADMIN no puede resetear la contraseña de un SUPERADMIN
        if (!esPropioUsuario && target.getRol() == Rol.SUPERADMIN
                && autenticado.getRol() != Rol.SUPERADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                    "ADMIN no puede cambiar la contraseña de un usuario SUPERADMIN");
        }

        if (esPropioUsuario) {
            if (request.passwordActual() == null || request.passwordActual().isBlank()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "PASSWORD_REQUIRED",
                        "Debes proporcionar tu contraseña actual para cambiarla");
            }
            if (!passwordEncoder.matches(request.passwordActual(), target.getPassword())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "WRONG_PASSWORD",
                        "La contraseña actual es incorrecta");
            }
        }

        target.setPassword(passwordEncoder.encode(request.passwordNuevo()));
        usuarioRepository.save(target);
        log.info("Contraseña cambiada para usuario: {} por {}", target.getEmail(),
                autenticado.getEmail());
    }

    // ─── Métodos auxiliares privados ──────────────────────────────────────────

    /**
     * Busca un usuario activo por ID o lanza {@link EntityNotFoundException}.
     *
     * @param id ID del usuario
     * @return entidad encontrada
     * @throws EntityNotFoundException si no existe usuario activo con ese ID
     */
    private Usuario findActivoOrThrow(UUID id) {
        return usuarioRepository.findById(id)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", id));
    }

    /**
     * Verifica que el usuario autenticado tenga acceso para operar sobre {@code target}.
     *
     * <p>Reglas:
     * <ul>
     *   <li>Cualquier usuario puede operar sobre sí mismo (para consulta y cambio de contraseña).</li>
     *   <li>ADMIN no puede operar sobre usuarios con rol SUPERADMIN.</li>
     *   <li>Roles distintos de ADMIN/SUPERADMIN no pueden operar sobre otros usuarios.</li>
     * </ul>
     *
     * @param target      usuario sobre el que se opera
     * @param autenticado usuario que realiza la operación
     * @param accion      descripción de la acción (para el mensaje de error)
     * @throws BusinessException 403 si el acceso no está permitido
     */
    private void verificarAccesoSobreTarget(Usuario target, Usuario autenticado, String accion) {
        boolean esPropioUsuario = target.getId().equals(autenticado.getId());
        if (esPropioUsuario) return;

        Rol rolAuth = autenticado.getRol();
        if (rolAuth != Rol.ADMIN && rolAuth != Rol.SUPERADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                    "No tienes permisos para " + accion + " otro usuario");
        }
        if (rolAuth == Rol.ADMIN && target.getRol() == Rol.SUPERADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                    "ADMIN no tiene permisos para " + accion + " un usuario SUPERADMIN");
        }
    }

    /**
     * Verifica que el usuario autenticado tenga permiso para asignar el rol solicitado.
     *
     * <p>Solo SUPERADMIN puede crear o asignar el rol SUPERADMIN.
     *
     * @param rolSolicitado rol que se intenta asignar
     * @param autenticado   usuario que realiza la operación
     * @param accion        descripción de la acción (para el mensaje de error)
     * @throws BusinessException 403 si ADMIN intenta asignar el rol SUPERADMIN
     */
    private void verificarPrivilegioRol(Rol rolSolicitado, Usuario autenticado, String accion) {
        if (rolSolicitado == Rol.SUPERADMIN && autenticado.getRol() != Rol.SUPERADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                    "Solo SUPERADMIN puede " + accion + " usuarios con rol SUPERADMIN");
        }
    }

    /**
     * Verifica que el email no esté en uso por ningún otro usuario.
     *
     * @param email    email a verificar
     * @param excluirId ID del usuario a excluir (para actualizaciones); {@code null} en creación
     * @throws BusinessException 409 si el email ya está registrado
     */
    private void verificarEmailUnico(String email, UUID excluirId) {
        String emailNorm = email.trim().toLowerCase();
        boolean existe = excluirId == null
                ? usuarioRepository.existsByEmail(emailNorm)
                : usuarioRepository.existsByEmailAndIdNot(emailNorm, excluirId);

        if (existe) {
            throw new BusinessException(HttpStatus.CONFLICT, "DUPLICATE_EMAIL",
                    "El email '" + emailNorm + "' ya está registrado en el sistema");
        }
    }
}
