package com.hospital.his.platform.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final AuthUserMapper mapper;

    public DatabaseUserDetailsService(AuthUserMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AuthUserAccount account = mapper.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        return new HospitalUserPrincipal(
                account.id(), account.username(), account.passwordHash(), account.displayName(),
                account.employeeId(), account.enabled(), mapper.findRolesByUserId(account.id()),
                mapper.findPermissionsByUserId(account.id()));
    }
}
