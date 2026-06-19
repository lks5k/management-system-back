package com.imsas.erp.config;

import com.imsas.erp.modules.usuarios.Usuario;
import com.imsas.erp.modules.usuarios.UsuarioRepository;
import com.imsas.erp.shared.enums.Rol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos exclusivo del perfil {@code dev}.
 *
 * <p>Se ejecuta una única vez al arrancar la aplicación en desarrollo.
 * Crea un usuario {@code SUPERADMIN} si no existe ninguno en la BD,
 * permitiendo hacer el primer login sin migraciones adicionales de datos.
 *
 * <p><strong>No se ejecuta en producción</strong> ({@code @Profile("dev")}).
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer implements CommandLineRunner {

    private static final String SUPERADMIN_EMAIL    = "admin@imsas.local";
    private static final String SUPERADMIN_PASSWORD = "Admin1234!";
    private static final String SUPERADMIN_NOMBRE   = "Super Administrador";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;

    /**
     * Crea el superadmin de desarrollo si no existe.
     * Imprime las credenciales en el log para facilitar el primer acceso.
     *
     * @param args argumentos de línea de comandos (no usados)
     */
    @Override
    public void run(String... args) {
        if (usuarioRepository.existsByEmail(SUPERADMIN_EMAIL)) {
            log.info("✓ Usuario SUPERADMIN ya existe: {}", SUPERADMIN_EMAIL);
            return;
        }

        Usuario superadmin = Usuario.builder()
                .nombre(SUPERADMIN_NOMBRE)
                .email(SUPERADMIN_EMAIL)
                .password(passwordEncoder.encode(SUPERADMIN_PASSWORD))
                .rol(Rol.SUPERADMIN)
                .build();

        usuarioRepository.save(superadmin);

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  Usuario SUPERADMIN creado para desarrollo:");
        log.info("  Email   : {}", SUPERADMIN_EMAIL);
        log.info("  Password: {}", SUPERADMIN_PASSWORD);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}
