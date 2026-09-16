package com.hospital.his.pharmacy.web;

import com.hospital.his.pharmacy.persistence.model.DrugStockTransactionRow;

import java.time.LocalDateTime;

public record DrugStockTransactionResponse(
        Long id,
        Long drugId,
        Long prescriptionId,
        String transactionType,
        int quantity,
        int quantityBefore,
        int quantityAfter,
        Long operatorUserId,
        LocalDateTime createdAt) {
    public static DrugStockTransactionResponse from(DrugStockTransactionRow row) {
        return new DrugStockTransactionResponse(
                row.id(),
                row.drugId(),
                row.prescriptionId(),
                row.transactionType(),
                row.quantity(),
                row.quantityBefore(),
                row.quantityAfter(),
                row.operatorUserId(),
                row.createdAt());
    }
}
