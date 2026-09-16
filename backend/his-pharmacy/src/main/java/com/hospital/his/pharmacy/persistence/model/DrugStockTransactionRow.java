package com.hospital.his.pharmacy.persistence.model;

import java.time.LocalDateTime;

public record DrugStockTransactionRow(
        Long id,
        Long drugId,
        Long prescriptionId,
        String transactionType,
        int quantity,
        int quantityBefore,
        int quantityAfter,
        Long operatorUserId,
        LocalDateTime createdAt) {
}
