package com.hospital.his.masterdata.persistence.model;

import java.math.BigDecimal;

public record MedicalTechnologyOption(
        Long id, String code, String name, String format, BigDecimal price, String type, Long departmentId) {
}
