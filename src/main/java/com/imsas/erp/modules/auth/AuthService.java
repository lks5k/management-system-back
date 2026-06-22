package com.imsas.erp.modules.auth;

import com.imsas.erp.modules.usuarios.Usuario;
import com.imsas.erp.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService            jwtService;

    
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
