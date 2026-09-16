package com.hospital.his.registration.persistence.model;

import java.math.BigDecimal;

public record RegistrationReferenceRow(
        Long employeeId,
        Long departmentId,
        Long registrationLevelId,
        String departmentName,
        String employeeName,
        String registrationLevelName,
        BigDecimal registrationFee,
        int registrationQuota) {
}
