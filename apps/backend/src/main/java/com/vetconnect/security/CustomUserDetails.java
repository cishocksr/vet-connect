package com.vetconnect.security;

import com.vetconnect.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Custom UserDetails implementation
 *
 * This wraps our User entity for Spring Security
 *
 * Spring Security needs:
 * - Username (we use email)
 * - Password
 * - Authorities (roles/permissions)
 * - Account status flags
 */
@AllArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails {

    private UUID id;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Create CustomUserDetails from User entity
     *
     * @param user User entity from database
     * @return CustomUserDetails for Spring Security
     */
    public static CustomUserDetails create(User user) {
        // For now, all users have USER role
        // In future, you can add admin roles, etc.
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                authorities
        );
    }

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
        return authorities;
    }

    // ========== ACCOUNT STATUS FLAGS ==========
    // All return true for now - can be customized later

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Accounts don't expire
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // We don't lock accounts (yet)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Passwords don't expire (yet)
    }

    @Override
    public boolean isEnabled() {
        return true;  // All accounts are enabled (could add email verification)
    }
}