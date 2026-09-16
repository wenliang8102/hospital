package com.hospital.his.outpatient.web.dto;

import com.hospital.his.outpatient.persistence.model.PrescriptionRow;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PrescriptionView(
        Long id, Long registrationId, Long drugId, String drugName, String drugFormat, String drugUnit,
        BigDecimal unitPrice, String drugUsage, Integer drugNumber, BigDecimal totalAmount,
        String state, LocalDateTime createdAt) {
    public static PrescriptionView from(PrescriptionRow row) {
        return new PrescriptionView(row.id(), row.registrationId(), row.drugId(), row.drugName(),
                row.drugFormat(), row.drugUnit(), row.unitPrice(), row.drugUsage(), row.drugNumber(),
                row.totalAmount(), row.state(), row.createdAt());
    }
}
