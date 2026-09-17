package com.hospital.his.pharmacy.web;

import com.hospital.his.pharmacy.persistence.model.DrugStockRow;

import java.time.LocalDateTime;

public record DrugStockResponse(
        Long drugId,
        String drugCode,
        String drugName,
        String drugFormat,
        String drugUnit,
        int quantity,
        long version,
        LocalDateTime updatedAt) {
    public static DrugStockResponse from(DrugStockRow row) {
        return new DrugStockResponse(
                row.drugId(), row.drugCode(), row.drugName(), row.drugFormat(), row.drugUnit(),
                row.quantity(), row.version(), row.updatedAt());
    }
}
