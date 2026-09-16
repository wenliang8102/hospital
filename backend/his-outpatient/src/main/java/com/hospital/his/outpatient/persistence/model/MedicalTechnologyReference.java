package com.hospital.his.outpatient.persistence.model;

import java.math.BigDecimal;

public record MedicalTechnologyReference(Long id, String name, String type, BigDecimal price) {
}
