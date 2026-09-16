package com.hospital.his.platform.admin;

public record UserAccountRow(
        Long id,
        String username,
        String displayName,
        Long employeeId,
        String employeeName,
        boolean enabled) {
}
