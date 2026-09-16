package com.hospital.his.registration.persistence.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ChargeItemRow(
        Long id, Long registrationId, String itemType, Long sourceId, String itemName,
        BigDecimal unitPrice, Integer quantity, BigDecimal totalAmount, String state,
        LocalDateTime paidAt, LocalDateTime createdAt, Long originalTransactionId) {
}
