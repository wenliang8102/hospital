package com.hospital.his.pharmacy.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DrugStockAdjustmentRequest(
        @NotNull Long drugId,
        @Min(0) @Max(999999) int targetQuantity) {
}
