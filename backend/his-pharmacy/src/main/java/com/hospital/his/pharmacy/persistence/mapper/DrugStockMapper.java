package com.hospital.his.pharmacy.persistence.mapper;

import com.hospital.his.pharmacy.persistence.model.DrugStockRow;
import com.hospital.his.pharmacy.persistence.model.DrugStockTransactionDraft;
import com.hospital.his.pharmacy.persistence.model.DrugStockTransactionRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DrugStockMapper {
    Optional<DrugStockRow> findByDrugId(Long drugId);

    int insertStock(@Param("drugId") Long drugId, @Param("quantity") int quantity);

    int changeQuantity(
            @Param("drugId") Long drugId,
            @Param("quantityDelta") int quantityDelta,
            @Param("expectedVersion") long expectedVersion);

    int setQuantity(
            @Param("drugId") Long drugId,
            @Param("quantity") int quantity,
            @Param("expectedVersion") long expectedVersion);

    int insertTransaction(DrugStockTransactionDraft transaction);

    List<DrugStockRow> findStocks(
            @Param("keyword") String keyword,
            @Param("keywordId") Long keywordId,
            @Param("maxQuantity") Integer maxQuantity,
            @Param("offset") long offset,
            @Param("limit") int limit);

    long countStocks(
            @Param("keyword") String keyword,
            @Param("keywordId") Long keywordId,
            @Param("maxQuantity") Integer maxQuantity);

    List<DrugStockTransactionRow> findTransactions(
            @Param("drugId") Long drugId,
            @Param("prescriptionId") Long prescriptionId,
            @Param("offset") long offset,
            @Param("limit") int limit);

    long countTransactions(@Param("drugId") Long drugId, @Param("prescriptionId") Long prescriptionId);
}
