package com.hospital.his.platform.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateUserRequest(
        @NotBlank @Size(max = 64) String username,
        @NotBlank @Size(min = 8, max = 128) String password,
        @NotBlank @Size(max = 64) String displayName,
        Long employeeId,
        @NotNull Boolean enabled,
        @NotEmpty List<@NotBlank @Size(max = 64) String> roleCodes) {
}
