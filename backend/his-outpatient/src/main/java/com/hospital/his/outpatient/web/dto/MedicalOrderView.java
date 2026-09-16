package com.hospital.his.outpatient.web.dto;

import com.hospital.his.outpatient.persistence.model.MedicalOrderRow;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MedicalOrderView(
        Long id, Long registrationId, String type, Long medicalTechnologyId, String itemName,
        BigDecimal price, String requestInfo, String bodyPosition, String remark,
        String state, LocalDateTime createdAt) {
    public static MedicalOrderView from(MedicalOrderRow row) {
        return new MedicalOrderView(row.id(), row.registrationId(), row.type(), row.medicalTechnologyId(),
                row.itemName(), row.price(), row.requestInfo(), row.bodyPosition(), row.remark(),
                row.state(), row.createdAt());
    }
}
