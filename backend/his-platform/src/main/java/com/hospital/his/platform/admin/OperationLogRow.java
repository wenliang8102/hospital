package com.hospital.his.platform.admin;

import java.time.LocalDateTime;

public record OperationLogRow(
        Long id,
        Long operatorId,
        String moduleCode,
        String action,
        String targetType,
        String targetId,
        String detail,
        LocalDateTime createdAt) {
}
