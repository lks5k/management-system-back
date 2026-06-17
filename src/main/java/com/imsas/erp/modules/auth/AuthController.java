package com.imsas.erp.modules.auth;

import com.imsas.erp.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de autenticación.
 *
 * <p>Ruta base: {@code /api/v1/auth}
 *
 * <p>Todos los endpoints de este controller son públicos (configurado en
 * {@code SecurityConfig}: {@code POST /api/v1/auth/**} → {@code permitAll()}).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Inicia sesión con email y contraseña.
     *
     * <p><b>Request:</b> {@code POST /api/v1/auth/login}
     * <pre>{@code
     * {
     *   "email": "usuario@imsas.com",
     *   "password": "mi_contraseña"
     * }
     * }</pre>
     *
     * <p><b>Response 200:</b>
     * <pre>{@code
     * {
     *   "data": {
     *     "token": "eyJhbGci...",
     *     "tipo": "Bearer",
     *     "id": "uuid",
     *     "nombre": "Juan Pérez",
     *     "email": "usuario@imsas.com",
     *     "rol": "ADMIN"
     *   },
     *   "meta": { "timestamp": "...", "version": "v1" },
     *   "errors": []
     * }
     * }</pre>
     *
     * <p><b>Response 401:</b> credenciales inválidas o usuario inactivo.
     * <p><b>Response 400:</b> email con formato inválido o campos vacíos.
     *
     * @param request DTO con email y contraseña
     * @return 200 con token JWT y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ApiResponse.ok(response);
    }
}
