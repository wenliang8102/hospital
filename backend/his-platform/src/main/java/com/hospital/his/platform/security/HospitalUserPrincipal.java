package com.hospital.his.platform.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record HospitalUserPrincipal(
        Long id,
        String username,
        String password,
        String displayName,
        Long employeeId,
        boolean enabled,
        List<String> roles,
        List<String> permissions) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return java.util.stream.Stream.concat(
                        roles.stream().map(role -> "ROLE_" + role),
                        permissions.stream())
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return password; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}
