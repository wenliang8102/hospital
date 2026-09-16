package com.hospital.his.registration.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreateRegistrationRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 64) String caseNumber,
        @NotBlank @Size(max = 64) String realName,
        @NotBlank @Pattern(regexp = "MALE|FEMALE|UNKNOWN") String gender,
        @Size(max = 32) String cardNumber,
        LocalDate birthday,
        @Min(0) @Max(150) Integer age,
        @Pattern(regexp = "YEAR|DAY") String ageType,
        @Size(max = 255) String homeAddress,
        @NotNull LocalDateTime visitDate,
        @NotBlank @Pattern(regexp = "AM|PM") String noon,
        @NotNull Long departmentId,
        @NotNull Long employeeId,
        @NotNull Long registrationLevelId,
        @NotNull Long settlementCategoryId,
        boolean booked,
        @NotBlank @Pattern(regexp = "CASH|BANK_CARD|WECHAT|ALIPAY|MEDICAL_INSURANCE") String registrationMethod) {
}
