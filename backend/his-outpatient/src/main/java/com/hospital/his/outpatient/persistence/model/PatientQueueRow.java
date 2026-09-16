package com.hospital.his.outpatient.persistence.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientQueueRow(
        Long id,
        String caseNumber,
        String realName,
        String gender,
        String cardNumber,
        LocalDate birthday,
        Integer age,
        String ageType,
        LocalDateTime visitDate,
        String noon,
        Long departmentId,
        String departmentName,
        Long employeeId,
        String employeeName,
        String registrationLevelName,
        String state,
        LocalDateTime createdAt) {
}
