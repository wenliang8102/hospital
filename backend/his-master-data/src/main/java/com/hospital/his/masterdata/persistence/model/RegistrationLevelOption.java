package com.hospital.his.masterdata.persistence.model;

import java.math.BigDecimal;

public record RegistrationLevelOption(Long id, String code, String name, BigDecimal fee, int quota) {
}
