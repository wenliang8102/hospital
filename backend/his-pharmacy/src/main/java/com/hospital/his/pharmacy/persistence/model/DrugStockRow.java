package com.hospital.his.pharmacy.persistence.model;

import java.time.LocalDateTime;

public record DrugStockRow(Long drugId, int quantity, long version, LocalDateTime updatedAt) {
}
