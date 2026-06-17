package com.imsas.erp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imsas.erp.shared.dto.ApiError;
import com.imsas.erp.shared.dto.ApiMeta;
import com.imsas.erp.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración principal de Spring Security.
 *
 * <h3>Decisiones de diseño</h3>
 * <ul>
 *   <li>CSRF deshabilitado: la API es stateless y los clientes son SPAs que
 *       no usan cookies de sesión.</li>
 *   <li>Sesión STATELESS: Spring Security nunca crea ni usa {@code HttpSession}.</li>
 *   <li>{@code @EnableMethodSecurity}: habilita {@code @PreAuthorize} en los
 *       controllers para control de acceso por rol a nivel de método.</li>
 *   <li>Los handlers de 401 y 403 retornan el envelope {@link ApiResponse}
 *       en JSON, manteniendo el contrato de la API.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter          jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper           objectMapper;

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     *
     * <p>Rutas públicas:
     * <ul>
     *   <li>{@code POST /api/v1/auth/**} — login y refresh token</li>
     *   <li>{@code GET /actuator/health} — health check para balanceadores</li>
     * </ul>
     *
     * @param http constructor de seguridad HTTP
     * @return cadena de filtros configurada
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(this::handleUnauthorized)
                        .accessDeniedHandler(this::handleForbidden)
                )
                .build();
    }

    /**
     * Proveedor de autenticación DAO que delega en {@link UserDetailsServiceImpl}
     * y usa BCrypt para verificar la contraseña.
     *
     * @return proveedor de autenticación configurado
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Expone el {@link AuthenticationManager} como bean para que
     * {@code AuthService} pueda invocarlo directamente al hacer login.
     *
     * @param config configuración de autenticación de Spring
     * @return gestor de autenticación
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Encoder BCrypt para contraseñas. Strength 10 (por defecto).
     *
     * @return encoder de contraseñas
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración CORS para el frontend (HTML + Alpine.js).
     *
     * <p>En desarrollo se permiten todos los orígenes ({@code *}).
     * En producción se debe restringir a los dominios del frontend
     * mediante variables de entorno. Configuración pendiente para prod.
     *
     * @return fuente de configuración CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ─── Handlers de error en formato envelope ────────────────────────────────

    /**
     * Devuelve 401 en formato JSON envelope cuando el token es inválido o ausente.
     * Evita que Spring Security retorne su página HTML de error por defecto.
     */
    private void handleUnauthorized(
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.AuthenticationException ex
    ) throws java.io.IOException {
        writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                "AUTHENTICATION_FAILED", "Autenticación requerida o credenciales inválidas");
    }

    /**
     * Devuelve 403 en formato JSON envelope cuando el usuario no tiene los permisos
     * necesarios para el recurso solicitado.
     */
    private void handleForbidden(
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException ex
    ) throws java.io.IOException {
        writeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                "ACCESS_DENIED", "No tienes permisos para realizar esta acción");
    }

    /**
     * Serializa un {@link ApiResponse} de error al body de la respuesta HTTP.
     *
     * @param response   respuesta HTTP a escribir
     * @param statusCode código HTTP (401 o 403)
     * @param code       código interno del error
     * @param message    mensaje descriptivo en español
     */
    private void writeErrorResponse(
            HttpServletResponse response,
            int statusCode,
            String code,
            String message
    ) throws java.io.IOException {
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .meta(ApiMeta.of("v1"))
                .errors(List.of(ApiError.of(code, message)))
                .build();

        objectMapper.writeValue(response.getWriter(), body);
    }
}
