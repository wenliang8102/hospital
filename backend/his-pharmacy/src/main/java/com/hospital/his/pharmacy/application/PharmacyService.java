package com.hospital.his.pharmacy.application;

import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.pharmacy.domain.PrescriptionState;
import com.hospital.his.pharmacy.persistence.mapper.DrugStockMapper;
import com.hospital.his.pharmacy.persistence.mapper.PrescriptionMapper;
import com.hospital.his.pharmacy.persistence.model.DrugStockRow;
import com.hospital.his.pharmacy.persistence.model.DrugStockTransactionDraft;
import com.hospital.his.pharmacy.persistence.model.PrescriptionRow;
import com.hospital.his.pharmacy.web.DrugStockResponse;
import com.hospital.his.pharmacy.web.DrugStockTransactionResponse;
import com.hospital.his.pharmacy.web.PharmacyPrescriptionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PharmacyService {
    private static final String TRANSACTION_DISPENSE = "DISPENSE";
    private static final String TRANSACTION_RETURN = "RETURN";
    private static final String TRANSACTION_INBOUND = "INBOUND";
    private static final String TRANSACTION_ADJUST_INCREASE = "ADJUST_INCREASE";
    private static final String TRANSACTION_ADJUST_DECREASE = "ADJUST_DECREASE";

    private final PrescriptionMapper prescriptionMapper;
    private final DrugStockMapper stockMapper;

    public PharmacyService(PrescriptionMapper prescriptionMapper, DrugStockMapper stockMapper) {
        this.prescriptionMapper = prescriptionMapper;
        this.stockMapper = stockMapper;
    }

    @Transactional(readOnly = true)
    public PageResult<PharmacyPrescriptionResponse> search(String keyword, PrescriptionState state, PageQuery page) {
        PrescriptionState targetState = state == null ? PrescriptionState.PAID : state;
        String normalizedKeyword = normalizeKeyword(keyword);
        Long keywordId = parseKeywordId(normalizedKeyword);
        long total = prescriptionMapper.countQueue(targetState.name(), normalizedKeyword, keywordId);
        if (total == 0) {
            return PageResult.of(List.of(), page, 0);
        }
        List<PharmacyPrescriptionResponse> items = prescriptionMapper
                .findQueue(targetState.name(), normalizedKeyword, keywordId, page.offset(), page.size())
                .stream()
                .map(PharmacyPrescriptionResponse::from)
                .toList();
        return PageResult.of(items, page, total);
    }

    @Transactional
    public void dispense(Long prescriptionId, Long operatorUserId, Long dispenserEmployeeId) {
        PrescriptionRow prescription = prescriptionMapper.findById(prescriptionId)
                .orElseThrow(PharmacyConflictException::invalidStateTransition);
        if (!PrescriptionState.PAID.name().equals(prescription.state())) {
            throw PharmacyConflictException.invalidStateTransition();
        }

        DrugStockRow stock = stockMapper.findByDrugId(prescription.drugId())
                .orElseThrow(PharmacyConflictException::insufficientStock);
        int quantityAfter = stock.quantity() - prescription.drugNumber();
        if (quantityAfter < 0) {
            throw PharmacyConflictException.insufficientStock();
        }

        int stockUpdated = stockMapper.changeQuantity(prescription.drugId(), -prescription.drugNumber(), stock.version());
        if (stockUpdated != 1) {
            throw PharmacyConflictException.insufficientStock();
        }

        stockMapper.insertTransaction(new DrugStockTransactionDraft(
                prescription.drugId(),
                prescription.id(),
                TRANSACTION_DISPENSE,
                prescription.drugNumber(),
                stock.quantity(),
                quantityAfter,
                operatorUserId));

        int prescriptionUpdated = prescriptionMapper.markDispensed(prescription.id(), dispenserEmployeeId);
        if (prescriptionUpdated != 1) {
            throw PharmacyConflictException.invalidStateTransition();
        }
    }

    @Transactional
    public void returnPrescription(Long prescriptionId, Long operatorUserId) {
        PrescriptionRow prescription = prescriptionMapper.findById(prescriptionId)
                .orElseThrow(PharmacyConflictException::invalidStateTransition);
        if (!PrescriptionState.DISPENSED.name().equals(prescription.state())) {
            throw PharmacyConflictException.invalidStateTransition();
        }

        DrugStockRow stock = stockMapper.findByDrugId(prescription.drugId())
                .orElseThrow(PharmacyConflictException::insufficientStock);
        int quantityAfter = stock.quantity() + prescription.drugNumber();
        int stockUpdated = stockMapper.changeQuantity(prescription.drugId(), prescription.drugNumber(), stock.version());
        if (stockUpdated != 1) {
            throw PharmacyConflictException.insufficientStock();
        }

        stockMapper.insertTransaction(new DrugStockTransactionDraft(
                prescription.drugId(),
                prescription.id(),
                TRANSACTION_RETURN,
                prescription.drugNumber(),
                stock.quantity(),
                quantityAfter,
                operatorUserId));

        int prescriptionUpdated = prescriptionMapper.markReturned(prescription.id());
        if (prescriptionUpdated != 1) {
            throw PharmacyConflictException.invalidStateTransition();
        }
    }

    @Transactional(readOnly = true)
    public PageResult<DrugStockResponse> searchStocks(String keyword, Integer maxQuantity, PageQuery page) {
        String normalizedKeyword = normalizeKeyword(keyword);
        Long keywordId = parseKeywordId(normalizedKeyword);

        Integer targetMaxQuantity = maxQuantity == null ? null : Math.max(maxQuantity, 0);
        long total = stockMapper.countStocks(normalizedKeyword, keywordId, targetMaxQuantity);
        if (total == 0) {
            return PageResult.of(List.of(), page, 0);
        }
        List<DrugStockResponse> items = stockMapper
                .findStocks(normalizedKeyword, keywordId, targetMaxQuantity, page.offset(), page.size())
                .stream()
                .map(DrugStockResponse::from)
                .toList();
        return PageResult.of(items, page, total);
    }

    @Transactional
    public void inboundStock(Long drugId, int quantity, Long operatorUserId) {
        DrugStockRow stock = stockMapper.findByDrugId(drugId).orElse(null);
        if (stock == null) {
            int inserted = stockMapper.insertStock(drugId, quantity);
            if (inserted != 1) {
                throw PharmacyConflictException.invalidStockOperation();
            }
            stockMapper.insertTransaction(new DrugStockTransactionDraft(
                    drugId,
                    null,
                    TRANSACTION_INBOUND,
                    quantity,
                    0,
                    quantity,
                    operatorUserId));
            return;
        }

        int quantityAfter = stock.quantity() + quantity;
        int stockUpdated = stockMapper.changeQuantity(drugId, quantity, stock.version());
        if (stockUpdated != 1) {
            throw PharmacyConflictException.invalidStockOperation();
        }
        stockMapper.insertTransaction(new DrugStockTransactionDraft(
                drugId,
                null,
                TRANSACTION_INBOUND,
                quantity,
                stock.quantity(),
                quantityAfter,
                operatorUserId));
    }

    @Transactional
    public void adjustStock(Long drugId, int targetQuantity, Long operatorUserId) {
        DrugStockRow stock = stockMapper.findByDrugId(drugId)
                .orElseThrow(PharmacyConflictException::invalidStockOperation);
        int delta = targetQuantity - stock.quantity();
        if (delta == 0) {
            return;
        }

        int stockUpdated = stockMapper.setQuantity(drugId, targetQuantity, stock.version());
        if (stockUpdated != 1) {
            throw PharmacyConflictException.invalidStockOperation();
        }
        stockMapper.insertTransaction(new DrugStockTransactionDraft(
                drugId,
                null,
                delta > 0 ? TRANSACTION_ADJUST_INCREASE : TRANSACTION_ADJUST_DECREASE,
                Math.abs(delta),
                stock.quantity(),
                targetQuantity,
                operatorUserId));
    }

    @Transactional(readOnly = true)
    public PageResult<DrugStockTransactionResponse> searchStockTransactions(
            Long drugId,
            Long prescriptionId,
            PageQuery page) {
        long total = stockMapper.countTransactions(drugId, prescriptionId);
        if (total == 0) {
            return PageResult.of(List.of(), page, 0);
        }
        List<DrugStockTransactionResponse> items = stockMapper
                .findTransactions(drugId, prescriptionId, page.offset(), page.size())
                .stream()
                .map(DrugStockTransactionResponse::from)
                .toList();
        return PageResult.of(items, page, total);
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }

    private Long parseKeywordId(String keyword) {
        if (keyword == null) {
            return null;
        }
        try {
            return Long.valueOf(keyword.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
