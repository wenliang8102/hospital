package com.hospital.his.registration.persistence.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RegistrationRow(
        Long id,
        String caseNumber,
        String realName,
        LocalDateTime visitDate,
        Long departmentId,
        Long employeeId,
        String visitState,
        BigDecimal registrationFee) {
}
