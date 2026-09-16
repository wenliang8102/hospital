package com.hospital.his.platform.admin;

import jakarta.validation.constraints.NotNull;

public record UserEnabledRequest(@NotNull Boolean enabled) {
}
