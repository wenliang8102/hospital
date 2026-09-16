package com.hospital.his.masterdata.persistence.model;

import java.math.BigDecimal;

public record MedicalTechnologyRow(
        Long id,
        String code,
        String name,
        String format,
        BigDecimal price,
        String type,
        String priceType,
        Long departmentId,
        String departmentName,
        boolean active) {
}
