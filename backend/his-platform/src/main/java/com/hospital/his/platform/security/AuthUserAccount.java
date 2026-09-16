package com.hospital.his.platform.security;

public record AuthUserAccount(
        Long id,
        String username,
        String passwordHash,
        String displayName,
        Long employeeId,
        boolean enabled) {
}
