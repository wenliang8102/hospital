package com.hospital.his.masterdata.persistence.model;

import java.math.BigDecimal;

public record EmployeeOption(
        Long id,
        String name,
        Long departmentId,
        Long registrationLevelId,
        String registrationLevelName,
        BigDecimal registrationFee) {
}
