package com.imsas.erp.modules.usuarios;

import com.imsas.erp.shared.entity.BaseEntity;
import com.imsas.erp.shared.enums.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Entidad que representa a un usuario del sistema.
 *
 * <p>Implementa {@link UserDetails} para integrarse directamente con Spring Security:
 * el email actúa como {@code username} y el rol determina las autoridades.
 *
 * <p>La contraseña se almacena hasheada con BCrypt en la columna {@code password_hash}.
 * Nunca se persiste en texto plano.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario extends BaseEntity implements UserDetails {

    /**
     * Nombre completo del usuario.
     */
    @Column(nullable = false, length = 120)
    private String nombre;

    /**
     * Correo electrónico. Actúa como nombre de usuario para la autenticación.
     * Único en el sistema.
     */
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    /**
     * Contraseña hasheada con BCrypt. Columna {@code password_hash}.
     * Nunca se expone en respuestas de la API.
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;

    /**
     * Rol del usuario. Determina permisos y accesos.
     * Se almacena como string para legibilidad y seguridad ante refactors del enum.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    // ─── UserDetails ─────────────────────────────────────────────────────────

    /**
     * Retorna las autoridades del usuario basadas en su rol.
     * Spring Security requiere el prefijo {@code ROLE_}.
     *
     * @return colección con una única autoridad {@code ROLE_{ROL}}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    /**
     * Retorna el email como identificador único del usuario para Spring Security.
     *
     * @return dirección de correo electrónico
     */
    @Override
    public String getUsername() {
        return email;
    }

    /** @return siempre {@code true} — la cuenta no expira en V1 */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** @return siempre {@code true} — no hay bloqueo automático en V1 */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** @return siempre {@code true} — las credenciales no expiran en V1 */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Un usuario está habilitado si y solo si su campo {@code activo} es {@code true}.
     * Spring Security rechazará la autenticación de usuarios desactivados.
     *
     * @return valor del campo {@code activo} heredado de {@link BaseEntity}
     */
    @Override
    public boolean isEnabled() {
        return isActivo();
    }
}
