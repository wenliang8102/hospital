package com.hospital.his.pharmacy.persistence.mapper;

import com.hospital.his.pharmacy.persistence.model.DrugStockRow;
import com.hospital.his.pharmacy.persistence.model.DrugStockTransactionDraft;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface DrugStockMapper {
    Optional<DrugStockRow> findByDrugId(Long drugId);

    int changeQuantity(
            @Param("drugId") Long drugId,
            @Param("quantityDelta") int quantityDelta,
            @Param("expectedVersion") long expectedVersion);

    int insertTransaction(DrugStockTransactionDraft transaction);
}
