package com.imsas.erp.modules.auth;

import com.imsas.erp.modules.usuarios.Usuario;
import com.imsas.erp.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Servicio de autenticación. Gestiona el flujo de login y generación de JWT.
 *
 * <p>Delega la validación de credenciales al {@link AuthenticationManager} de Spring
 * Security, que internamente usa {@code UserDetailsServiceImpl} y BCrypt. Si las
 * credenciales son incorrectas o el usuario está inactivo, el manager lanza una
 * {@code AuthenticationException} que el {@code GlobalExceptionHandler} convierte
 * en una respuesta 401.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService            jwtService;

    /**
     * Autentica al usuario con email y contraseña y genera un token JWT.
     *
     * <p>Flujo:
     * <ol>
     *   <li>Delega en {@link AuthenticationManager#authenticate} con un
     *       {@link UsernamePasswordAuthenticationToken}.</li>
     *   <li>Si las credenciales son válidas, el manager retorna el principal autenticado.</li>
     *   <li>Se genera el JWT con el claim {@code rol} incluido.</li>
     *   <li>Se construye y retorna el {@link LoginResponse}.</li>
     * </ol>
     *
     * @param request DTO con email y contraseña validados por JSR-380
     * @return respuesta con token JWT y datos del usuario
     * @throws org.springframework.security.core.AuthenticationException si las
     *         credenciales son incorrectas o el usuario está inactivo
     */
    public LoginResponse login(LoginRequest request) {
        var authToken = new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
        );

        // Lanza AuthenticationException si las credenciales son inválidas
        var authentication = authenticationManager.authenticate(authToken);
        var usuario = (Usuario) authentication.getPrincipal();

        String jwt = jwtService.generateToken(usuario);

        log.info("Login exitoso para usuario: {} [{}]", usuario.getEmail(), usuario.getRol());

        return LoginResponse.builder()
                .token(jwt)
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .build();
    }
}
