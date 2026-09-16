package com.hospital.his.masterdata.persistence.model;

import java.math.BigDecimal;

public record DrugOption(
        Long id, String code, String name, String format, String unit, String dosage,
        String type, BigDecimal price, String manufacturer) {
}
