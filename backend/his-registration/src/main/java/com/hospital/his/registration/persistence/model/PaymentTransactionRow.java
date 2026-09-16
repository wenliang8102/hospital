package com.hospital.his.registration.persistence.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentTransactionRow(
        Long id, String transactionNo, Long registrationId, String transactionType,
        String paymentMethod, BigDecimal amount, String status, Long operatorUserId,
        Long originalTransactionId, String reason, LocalDateTime createdAt) {
}
