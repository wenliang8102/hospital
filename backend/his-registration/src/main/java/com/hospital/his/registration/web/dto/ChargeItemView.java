package com.hospital.his.registration.web.dto;

import com.hospital.his.registration.persistence.model.ChargeItemRow;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ChargeItemView(
        Long id, Long registrationId, String itemType, Long sourceId, String itemName,
        BigDecimal unitPrice, Integer quantity, BigDecimal totalAmount, String state,
        LocalDateTime paidAt, LocalDateTime createdAt, Long originalTransactionId) {
    public static ChargeItemView from(ChargeItemRow row) {
        return new ChargeItemView(row.id(), row.registrationId(), row.itemType(), row.sourceId(),
                row.itemName(), row.unitPrice(), row.quantity(), row.totalAmount(), row.state(),
                row.paidAt(), row.createdAt(), row.originalTransactionId());
    }
}
