package com.hospital.his.medicaltech.web;

import com.hospital.his.medicaltech.domain.MedicalOrderState;
import com.hospital.his.medicaltech.domain.MedicalOrderType;
import com.hospital.his.medicaltech.persistence.model.MedicalOrderRow;

import java.time.LocalDateTime;

public record MedicalOrderResponse(
        Long id,
        MedicalOrderType type,
        Long registrationId,
        Long medicalTechnologyId,
        String requestInfo,
        String bodyPosition,
        Long executorEmployeeId,
        Long resultEmployeeId,
        LocalDateTime executedAt,
        String result,
        MedicalOrderState state,
        String remark,
        LocalDateTime createdAt) {
    public static MedicalOrderResponse from(MedicalOrderType type, MedicalOrderRow row) {
        return new MedicalOrderResponse(
                row.id(),
                type,
                row.registrationId(),
                row.medicalTechnologyId(),
                row.requestInfo(),
                row.bodyPosition(),
                row.executorEmployeeId(),
                row.resultEmployeeId(),
                row.executedAt(),
                row.result(),
                MedicalOrderState.valueOf(row.state()),
                row.remark(),
                row.createdAt());
    }
}
