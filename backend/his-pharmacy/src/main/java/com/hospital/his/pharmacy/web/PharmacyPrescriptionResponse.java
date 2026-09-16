package com.hospital.his.pharmacy.web;

import com.hospital.his.pharmacy.domain.PrescriptionState;
import com.hospital.his.pharmacy.persistence.model.PrescriptionRow;

import java.time.LocalDateTime;

public record PharmacyPrescriptionResponse(
        Long id,
        Long registrationId,
        Long drugId,
        String drugUsage,
        int drugNumber,
        PrescriptionState state,
        LocalDateTime createdAt,
        LocalDateTime dispensedAt,
        Long dispensedBy,
        Integer stockQuantity) {
    public static PharmacyPrescriptionResponse from(PrescriptionRow row) {
        return new PharmacyPrescriptionResponse(
                row.id(),
                row.registrationId(),
                row.drugId(),
                row.drugUsage(),
                row.drugNumber(),
                PrescriptionState.valueOf(row.state()),
                row.createdAt(),
                row.dispensedAt(),
                row.dispensedBy(),
                row.stockQuantity());
    }
}
