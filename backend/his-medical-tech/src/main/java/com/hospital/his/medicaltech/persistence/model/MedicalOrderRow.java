package com.hospital.his.medicaltech.persistence.model;

import java.time.LocalDateTime;

public record MedicalOrderRow(
        Long id,
        Long registrationId,
        Long medicalTechnologyId,
        String requestInfo,
        String bodyPosition,
        Long executorEmployeeId,
        Long resultEmployeeId,
        LocalDateTime executedAt,
        String result,
        String state,
        String remark,
        LocalDateTime createdAt) {
}
