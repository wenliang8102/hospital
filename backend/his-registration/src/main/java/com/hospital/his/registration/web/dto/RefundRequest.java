package com.hospital.his.registration.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RefundRequest(
        @NotNull Long originalTransactionId,
        @NotNull @Size(min = 1, max = 100) List<@NotNull Long> chargeItemIds,
        @NotBlank @Size(max = 500) String reason,
        @NotBlank @Size(max = 64) String idempotencyKey) {
}
