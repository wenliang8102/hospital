package com.hospital.his.pharmacy.persistence.model;

import java.time.LocalDateTime;

public record PrescriptionRow(
        Long id,
        Long registrationId,
        Long drugId,
        String drugUsage,
        int drugNumber,
        String state,
        LocalDateTime createdAt,
        LocalDateTime dispensedAt,
        Long dispensedBy,
        Integer stockQuantity) {
}
