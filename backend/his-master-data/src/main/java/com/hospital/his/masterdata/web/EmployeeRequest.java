package com.hospital.his.masterdata.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmployeeRequest(
        @NotBlank @Size(max = 64) String realName,
        @NotNull Long departmentId,
        Long registLevelId,
        Long schedulingId,
        @NotNull Boolean active) {
}
