package com.hospital.his.outpatient.persistence.model;

import java.math.BigDecimal;

public record DrugReference(Long id, String name, String format, String unit, BigDecimal price) {
}
