package com.inspectron.inspectron.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inspectron.inspectron.domain.user.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID id;

    @Column(length = 30, nullable = false, unique = true)
    private String name;

    @Column(length = 20, nullable = false, unique = true)
    private String enrollment;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private UserRole role;

    @Column(length = 200, unique = true)
    private String email;

    @Builder.Default
    @Column(nullable = false)
    private boolean disabled = false;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(name = "first_access", nullable = false)
    private boolean firstAccess = true;

    @Builder.Default
    @Column(name = "user_tutorial", nullable = false)
    private boolean tutorial = true;

    @JsonIgnore
    @Column(nullable = false)
    private String salt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public void encodePassword(String rawPassword) {
        this.salt = BCrypt.gensalt();
        this.password = BCrypt.hashpw(rawPassword, this.salt);
    }

    public boolean matchesPassword(String rawPassword) {
        return this.password != null && BCrypt.checkpw(rawPassword, this.password);
    }

    public boolean isDefaultPassword() {
        return this.enrollment != null && matchesPassword(this.enrollment);
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return this.enrollment;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return !this.disabled;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !this.disabled;
    }
}
