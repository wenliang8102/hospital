package com.hospital.his.registration.persistence.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RegistrationRow(
        Long id,
        String requestNo,
        String caseNumber,
        String realName,
        String gender,
        String cardNumber,
        LocalDate birthday,
        Integer age,
        String ageType,
        String homeAddress,
        LocalDateTime visitDate,
        String noon,
        Long departmentId,
        String departmentName,
        Long employeeId,
        String employeeName,
        Long registrationLevelId,
        String registrationLevelName,
        Long settlementCategoryId,
        String settlementCategoryName,
        boolean booked,
        String registrationMethod,
        String visitState,
        BigDecimal registrationFee,
        LocalDateTime createdAt) {
}
