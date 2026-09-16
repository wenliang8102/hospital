package com.hospital.his.outpatient.web.dto;

import com.hospital.his.outpatient.persistence.model.PatientQueueRow;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientView(
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

    public static PatientView from(PatientQueueRow row) {
        return new PatientView(row.id(), row.caseNumber(), row.realName(), row.gender(), row.cardNumber(),
                row.birthday(), row.age(), row.ageType(), row.visitDate(), row.noon(), row.departmentId(),
                row.departmentName(), row.employeeId(), row.employeeName(), row.registrationLevelName(),
                row.state(), row.createdAt());
    }
}
