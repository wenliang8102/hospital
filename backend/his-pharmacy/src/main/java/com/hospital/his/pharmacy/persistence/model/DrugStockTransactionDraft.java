package com.hospital.his.pharmacy.persistence.model;

public record DrugStockTransactionDraft(
        Long drugId,
        Long prescriptionId,
        String transactionType,
        int quantity,
        int quantityBefore,
        int quantityAfter,
        Long operatorUserId) {
}
