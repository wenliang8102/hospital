package com.hospital.his.pharmacy.persistence.model;

import java.time.LocalDateTime;

public record PrescriptionRow(
        Long id,
        Long registrationId,
        Long drugId,
        String drugCode,
        String drugName,
        String drugFormat,
        String drugUnit,
        String drugUsage,
        int drugNumber,
        String state,
        LocalDateTime createdAt,
        LocalDateTime dispensedAt,
        Long dispensedBy,
        Integer stockQuantity) {
}
