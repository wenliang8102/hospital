package com.hospital.his.outpatient.persistence.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PrescriptionRow(
        Long id, Long registrationId, Long drugId, String drugName, String drugFormat, String drugUnit,
        BigDecimal unitPrice, String drugUsage, Integer drugNumber, BigDecimal totalAmount,
        String state, LocalDateTime createdAt) {
}
