package com.hospital.his.pharmacy.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DrugStockInboundRequest(
        @NotNull Long drugId,
        @Min(1) @Max(999999) int quantity) {
}
