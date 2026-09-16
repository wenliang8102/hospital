package com.hospital.his.masterdata.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 64) String name,
        @NotBlank @Size(max = 32) String type,
        @NotNull Boolean active) {
}
