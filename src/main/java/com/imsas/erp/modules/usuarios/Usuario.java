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

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario extends BaseEntity implements UserDetails {

    
    @Column(nullable = false, length = 120)
    private String nombre;

    
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    // ─── UserDetails ─────────────────────────────────────────────────────────

    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    
    @Override
    public String getUsername() {
        return email;
    }

    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    
    @Override
    public boolean isEnabled() {
        return isActivo();
    }
}
