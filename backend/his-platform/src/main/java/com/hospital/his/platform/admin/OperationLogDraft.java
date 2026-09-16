package com.hospital.his.platform.admin;

public record OperationLogDraft(
        Long operatorId,
        String moduleCode,
        String action,
        String targetType,
        String targetId,
        String detail) {
}
