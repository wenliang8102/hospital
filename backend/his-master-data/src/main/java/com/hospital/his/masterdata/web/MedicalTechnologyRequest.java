package com.hospital.his.masterdata.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MedicalTechnologyRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 128) String name,
        @Size(max = 64) String format,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotBlank @Size(max = 32) String type,
        @Size(max = 64) String priceType,
        @NotNull Long departmentId,
        @NotNull Boolean active) {
}
