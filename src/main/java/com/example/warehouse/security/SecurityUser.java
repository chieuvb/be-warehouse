package com.example.warehouse.security;

import com.example.warehouse.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * An adapter class that wraps the User entity to provide it to Spring Security.
 * This decouples the core domain entity from the security framework.
 */
@AllArgsConstructor
public class SecurityUser implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        // For now, accounts do not expire. This can be tied to a user field later.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // For now, accounts are not locked. This can be tied to a user field later.
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // For now, credentials do not expire. This can be tied to a user field later.
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getIsActive();
    }
}
