package io.github.kaltrinabajramii.urbantransitbackend.security;

import io.github.kaltrinabajramii.urbantransitbackend.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

/**
 * This class wraps our User entity to work with Spring Security
 * Spring Security needs UserDetails interface for authentication
 */
@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String email;
    private String fullName;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean active;

    /**
     * CONVERTS our User entity to Spring Security's UserDetails
     * This is called when Spring Security needs user information
     */
    public static UserPrincipal create(User user) {
        // Convert user role to Spring Security authority
        Collection<GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPasswordHash(),
                authorities,
                user.getActive()
        );
    }

    // ===== Spring Security Required Methods =====

    @Override
    public String getUsername() {
        return email;  // We use email as username
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;  // User roles (ROLE_USER, ROLE_ADMIN)
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // We don't expire accounts
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // We don't lock accounts
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // We don't expire passwords
    }

    @Override
    public boolean isEnabled() {
        return active;  // Use our active field
    }
}