package com.hospital.his.registration.web.dto;

import com.hospital.his.registration.persistence.model.RegistrationRow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RegistrationView(
        Long id,
        String requestId,
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
        String state,
        BigDecimal registrationFee,
        LocalDateTime createdAt) {

    public static RegistrationView from(RegistrationRow row) {
        return new RegistrationView(
                row.id(), row.requestNo(), row.caseNumber(), row.realName(), row.gender(), row.cardNumber(),
                row.birthday(), row.age(), row.ageType(), row.homeAddress(), row.visitDate(), row.noon(),
                row.departmentId(), row.departmentName(), row.employeeId(), row.employeeName(),
                row.registrationLevelId(), row.registrationLevelName(), row.settlementCategoryId(),
                row.settlementCategoryName(), row.booked(), row.registrationMethod(), row.visitState(),
                row.registrationFee(), row.createdAt());
    }
}
