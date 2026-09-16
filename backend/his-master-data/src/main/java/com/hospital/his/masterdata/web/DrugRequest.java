package com.hospital.his.masterdata.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DrugRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String format,
        @NotBlank @Size(max = 16) String unit,
        @Size(max = 255) String manufacturer,
        @Size(max = 64) String dosage,
        @Size(max = 64) String type,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @Size(max = 64) String mnemonicCode,
        @NotNull Boolean active) {
}
