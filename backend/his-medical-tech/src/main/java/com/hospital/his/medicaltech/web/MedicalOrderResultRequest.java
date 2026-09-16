package com.hospital.his.medicaltech.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicalOrderResultRequest(
        @NotBlank
        String result,
        @Size(max = 1000)
        String remark) {
}
