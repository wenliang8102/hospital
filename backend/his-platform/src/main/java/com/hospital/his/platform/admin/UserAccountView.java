package com.hospital.his.platform.admin;

import java.util.List;

public record UserAccountView(
        Long id,
        String username,
        String displayName,
        Long employeeId,
        String employeeName,
        boolean enabled,
        List<RoleView> roles) {
    public UserAccountView {
        roles = List.copyOf(roles);
    }
}
