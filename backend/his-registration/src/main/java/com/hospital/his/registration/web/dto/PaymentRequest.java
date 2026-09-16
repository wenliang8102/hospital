package com.hospital.his.registration.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PaymentRequest(
        @NotNull Long registrationId,
        @NotNull @Size(min = 1, max = 100) List<@NotNull Long> chargeItemIds,
        @NotBlank @Pattern(regexp = "CASH|BANK_CARD|WECHAT|ALIPAY|MEDICAL_INSURANCE") String paymentMethod,
        @NotBlank @Size(max = 64) String idempotencyKey) {
}
