package org.avyla.security.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.avyla.vehicles.domain.enums.DocumentIdentityType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    // === Credenciales ===

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    // === Datos personales ===

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_identity_type", nullable = false, length = 10)
    private DocumentIdentityType documentType;

    @Column(name = "document_number", nullable = false, length = 20)
    private String documentNumber;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // === Estado y seguridad ===

    @Column(name = "is_enable", nullable = false)
    private Boolean isEnable = true;

    @Column(name = "is_account_no_locked", nullable = false)
    private Boolean isAccountNoLocked = true;

    @Column(name = "is_credentials_non_expired", nullable = false)
    private Boolean isCredentialsNonExpired = true;

    @Column(name = "is_account_non_expired", nullable = false)
    private Boolean isAccountNonExpired = true;

    // === Soft Delete ===

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // === Relaciones ===

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    private List<String> vehicles;

    // === Métodos de Soft Delete ===

    /**
     * Marca el usuario como eliminado (soft delete)
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isEnable = false;
    }

    /**
     * Restaura un usuario eliminado
     */
    public void restore() {
        this.deletedAt = null;
        this.isEnable = true;
    }

    /**
     * Verifica si el usuario está eliminado
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // === UserDetails Implementation ===

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.isAccountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isAccountNoLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.isCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnable && !isDeleted();
    }
}