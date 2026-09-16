package com.hospital.his.masterdata.persistence.model;

import java.math.BigDecimal;

public record DrugRow(
        Long id,
        String code,
        String name,
        String format,
        String unit,
        String manufacturer,
        String dosage,
        String type,
        BigDecimal price,
        String mnemonicCode,
        boolean active) {
}
