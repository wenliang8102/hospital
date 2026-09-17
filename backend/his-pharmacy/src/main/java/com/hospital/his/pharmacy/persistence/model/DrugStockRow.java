package com.hospital.his.pharmacy.persistence.model;

import java.time.LocalDateTime;

public record DrugStockRow(
        Long drugId,
        String drugCode,
        String drugName,
        String drugFormat,
        String drugUnit,
        int quantity,
        long version,
        LocalDateTime updatedAt) {
}
