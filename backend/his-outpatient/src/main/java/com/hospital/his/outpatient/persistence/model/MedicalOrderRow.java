package com.hospital.his.outpatient.persistence.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MedicalOrderRow(
        Long id, Long registrationId, String type, Long medicalTechnologyId, String itemName,
        BigDecimal price, String requestInfo, String bodyPosition, String remark,
        String state, LocalDateTime createdAt) {
}
