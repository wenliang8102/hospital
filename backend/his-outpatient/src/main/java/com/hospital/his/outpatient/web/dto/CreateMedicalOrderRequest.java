package com.hospital.his.outpatient.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateMedicalOrderRequest(
        @NotBlank @Pattern(regexp = "CHECK|INSPECTION|DISPOSAL") String type,
        @NotNull Long medicalTechnologyId,
        @Size(max = 1000) String requestInfo,
        @Size(max = 255) String bodyPosition,
        @Size(max = 1000) String remark) {
}
