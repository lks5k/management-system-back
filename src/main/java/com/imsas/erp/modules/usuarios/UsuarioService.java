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

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;

    // ─── Consulta ────────────────────────────────────────────────────────────

    
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable, Usuario autenticado) {
        Page<Usuario> pagina = autenticado.getRol() == Rol.SUPERADMIN
                ? usuarioRepository.findAllByActivoTrue(pageable)
                : usuarioRepository.findAllByActivoTrueAndRolNot(Rol.SUPERADMIN, pageable);

        return pagina.map(UsuarioResponse::from);
    }

    
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(UUID id, Usuario autenticado) {
        Usuario target = findActivoOrThrow(id);
        verificarAccesoSobreTarget(target, autenticado, "consultar");
        return UsuarioResponse.from(target);
    }

    // ─── Creación ─────────────────────────────────────────────────────────────

    
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request, Usuario autenticado) {
        verificarPrivilegioRol(request.rol(), autenticado, "crear");
        verificarEmailUnico(request.email(), null);

        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PASSWORD_REQUIRED",
                    "La contraseña inicial es obligatoria al crear un usuario");
        }

        Usuario nuevo = Usuario.builder()
                .nombre(request.nombre().trim())
                .email(request.email().trim().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .rol(request.rol())
                .build();

        Usuario guardado = usuarioRepository.save(nuevo);
        log.info("Usuario creado: {} [{}] por {}", guardado.getEmail(), guardado.getRol(),
                autenticado.getEmail());

        return UsuarioResponse.from(guardado);
    }

    // ─── Actualización ────────────────────────────────────────────────────────

    
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

    // ─── Toggle activo/inactivo ───────────────────────────────────────────────

    
    @Transactional
    public UsuarioResponse cambiarEstado(UUID id, CambiarEstadoUsuarioRequest request, Usuario autenticado) {
        Usuario target = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", id));

        if (!request.activo() && id.equals(autenticado.getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "SELF_DEACTIVATION",
                    "No puedes desactivar tu propia cuenta");
        }

        verificarAccesoSobreTarget(target, autenticado, "modificar estado de");
        target.setActivo(request.activo());
        Usuario guardado = usuarioRepository.save(target);
        log.info("Estado de usuario {} → {} por {}", target.getEmail(), request.activo(), autenticado.getEmail());
        return UsuarioResponse.from(guardado);
    }

    // ─── Soft delete ──────────────────────────────────────────────────────────

    
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

    
    private Usuario findActivoOrThrow(UUID id) {
        return usuarioRepository.findById(id)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", id));
    }

    
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

    
    private void verificarPrivilegioRol(Rol rolSolicitado, Usuario autenticado, String accion) {
        if (rolSolicitado == Rol.SUPERADMIN && autenticado.getRol() != Rol.SUPERADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                    "Solo SUPERADMIN puede " + accion + " usuarios con rol SUPERADMIN");
        }
    }

    
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
