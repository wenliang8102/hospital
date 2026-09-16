package com.hospital.his.pharmacy.web;

import com.hospital.his.pharmacy.persistence.model.DrugStockRow;

import java.time.LocalDateTime;

public record DrugStockResponse(
        Long drugId,
        int quantity,
        long version,
        LocalDateTime updatedAt) {
    public static DrugStockResponse from(DrugStockRow row) {
        return new DrugStockResponse(row.drugId(), row.quantity(), row.version(), row.updatedAt());
    }
}
