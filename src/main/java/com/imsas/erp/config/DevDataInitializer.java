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
