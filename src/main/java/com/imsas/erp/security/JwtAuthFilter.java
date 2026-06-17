package com.imsas.erp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT que se ejecuta una sola vez por request ({@link OncePerRequestFilter}).
 *
 * <p>Flujo de procesamiento:
 * <ol>
 *   <li>Lee el header {@code Authorization: Bearer {token}}.</li>
 *   <li>Si no hay header o no empieza con {@code "Bearer "}, pasa al siguiente filtro sin autenticar.</li>
 *   <li>Extrae el email del token sin validar la firma aún (solo parseo del subject).</li>
 *   <li>Si el SecurityContext ya tiene una autenticación, no sobreescribe.</li>
 *   <li>Carga el {@link UserDetails} desde la BD y valida el token completo.</li>
 *   <li>Si es válido, establece la autenticación en el {@link SecurityContextHolder}.</li>
 * </ol>
 *
 * <p>Cualquier error de token (expirado, firma inválida) no lanza excepción aquí:
 * simplemente no se establece la autenticación y Spring Security devuelve 401
 * automáticamente para los endpoints protegidos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER   = "Authorization";

    private final JwtService            jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Procesa cada request HTTP para extraer y validar el token JWT.
     *
     * @param request     solicitud HTTP entrante
     * @param response    respuesta HTTP
     * @param filterChain cadena de filtros de Spring Security
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(BEARER_PREFIX.length());

        // Extraer username sin lanzar excepción; si falla, token es inválido
        String username = null;
        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            log.debug("No se pudo extraer username del token: {}", e.getMessage());
        }

        // Solo autenticar si hay username y el SecurityContext aún no tiene autenticación
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Usuario autenticado vía JWT: {}", username);
            }
        }

        filterChain.doFilter(request, response);
    }
}
