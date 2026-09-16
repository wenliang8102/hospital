package com.hospital.his.outpatient.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePrescriptionRequest(
        @NotNull Long drugId,
        @NotBlank @Size(max = 500) String drugUsage,
        @Min(1) @Max(10000) int drugNumber) {
}
