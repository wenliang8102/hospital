package com.hospital.his.masterdata.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SchedulingRequest(
        @NotBlank @Size(max = 64) String name,
        @NotBlank @Pattern(regexp = "^[01]{14}$", message = "必须是 14 位 0/1 周规则") String weekRule,
        @NotNull Boolean active) {
}
