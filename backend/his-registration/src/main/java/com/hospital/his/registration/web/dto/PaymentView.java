package com.hospital.his.registration.web.dto;

import com.hospital.his.registration.persistence.model.PaymentTransactionRow;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentView(
        Long id, String transactionNo, Long registrationId, String transactionType,
        String paymentMethod, BigDecimal amount, String status, Long originalTransactionId,
        String reason, LocalDateTime createdAt) {
    public static PaymentView from(PaymentTransactionRow row) {
        return new PaymentView(row.id(), row.transactionNo(), row.registrationId(), row.transactionType(),
                row.paymentMethod(), row.amount(), row.status(), row.originalTransactionId(),
                row.reason(), row.createdAt());
    }
}
