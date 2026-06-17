package com.imsas.erp.security;

import com.imsas.erp.modules.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link UserDetailsService} que carga usuarios desde la BD.
 *
 * <p>Spring Security llama a este servicio durante la autenticación básica y
 * durante la validación del token JWT en {@link JwtAuthFilter}.
 *
 * <p>Solo carga usuarios con {@code activo = true}: un usuario desactivado
 * (soft delete) no puede autenticarse, ya que {@code Usuario#isEnabled()} retorna
 * {@code false} cuando {@code activo = false} y Spring Security rechaza el login.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carga un usuario activo por su email.
     *
     * @param email dirección de correo del usuario (usado como username)
     * @return detalles del usuario para Spring Security
     * @throws UsernameNotFoundException si no existe un usuario activo con ese email
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Cargando usuario por email: {}", email);
        return usuarioRepository.findByEmailAndActivoTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado o inactivo: " + email));
    }
}
