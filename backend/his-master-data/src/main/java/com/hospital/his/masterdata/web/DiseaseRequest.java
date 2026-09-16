package com.hospital.his.masterdata.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DiseaseRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 50) String icd,
        @Size(max = 50) String category,
        @NotNull Boolean active) {
}
