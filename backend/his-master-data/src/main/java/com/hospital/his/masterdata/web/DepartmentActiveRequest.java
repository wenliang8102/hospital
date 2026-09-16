package com.hospital.his.masterdata.web;

import jakarta.validation.constraints.NotNull;

public record DepartmentActiveRequest(@NotNull Boolean active) {
}
