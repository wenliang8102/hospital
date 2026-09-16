package com.hospital.his.platform.security;

import java.util.List;

public record UserProfile(
        Long id,
        String username,
        String displayName,
        Long employeeId,
        List<String> roles,
        List<String> permissions) {
    public static UserProfile from(HospitalUserPrincipal principal) {
        return new UserProfile(principal.id(), principal.username(), principal.displayName(),
                principal.employeeId(), principal.roles(), principal.permissions());
    }
}
