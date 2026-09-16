package com.hospital.his.registration.application;

import com.hospital.his.common.exception.BusinessException;
import com.hospital.his.registration.persistence.mapper.BillingMapper;
import com.hospital.his.registration.persistence.model.ChargeItemRow;
import com.hospital.his.registration.persistence.model.PaymentTransactionDraft;
import com.hospital.his.registration.persistence.model.PaymentTransactionRow;
import com.hospital.his.registration.web.dto.ChargeItemView;
import com.hospital.his.registration.web.dto.PaymentRequest;
import com.hospital.his.registration.web.dto.PaymentView;
import com.hospital.his.registration.web.dto.RefundRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BillingService {
    private final BillingMapper mapper;

    public BillingService(BillingMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ChargeItemView> chargeItems(Long registrationId, String state) {
        String normalizedState = state == null || state.isBlank() ? null : state;
        return mapper.findChargeItems(registrationId, normalizedState).stream()
                .map(ChargeItemView::from)
                .toList();
    }

    @Transactional
    public PaymentView pay(PaymentRequest request, Long operatorUserId) {
        List<Long> ids = distinctIds(request.chargeItemIds());
        var existing = mapper.findTransactionByNo(request.idempotencyKey());
        if (existing.isPresent()) {
            return requireSameRequest(existing.get(), "PAYMENT", request.registrationId(), null,
                    request.paymentMethod(), null, ids);
        }

        List<ChargeItemRow> items = mapper.lockChargeItems(ids);
        requireChargeItems(items, ids, request.registrationId(), "UNPAID");
        BigDecimal amount = total(items);
        PaymentTransactionDraft transaction = new PaymentTransactionDraft(
                request.idempotencyKey(), request.registrationId(), "PAYMENT", request.paymentMethod(),
                amount, operatorUserId, null, null);
        mapper.insertTransaction(transaction);
        mapper.insertTransactionItems(transaction.getId(), items);
        if (mapper.markPaid(ids) != ids.size()) {
            throw invalidState("收费项状态已发生变化，请刷新后重试");
        }
        items.forEach(item -> syncSource(item, true));
        return PaymentView.from(mapper.findTransactionById(transaction.getId()).orElseThrow());
    }

    @Transactional
    public PaymentView refund(RefundRequest request, Long operatorUserId) {
        List<Long> ids = distinctIds(request.chargeItemIds());
        var existing = mapper.findTransactionByNo(request.idempotencyKey());
        if (existing.isPresent()) {
            return requireSameRequest(existing.get(), "REFUND", existing.get().registrationId(),
                    request.originalTransactionId(), existing.get().paymentMethod(), request.reason().trim(), ids);
        }
        PaymentTransactionRow original = mapper.findTransactionById(request.originalTransactionId())
                .filter(row -> "PAYMENT".equals(row.transactionType()) && "SUCCESS".equals(row.status()))
                .orElseThrow(() -> new BusinessException(
                        "RESOURCE_NOT_FOUND", "原支付流水不存在", HttpStatus.NOT_FOUND));

        List<ChargeItemRow> items = mapper.lockChargeItems(ids);
        requireChargeItems(items, ids, original.registrationId(), "PAID");
        if (mapper.countTransactionItems(original.id(), ids) != ids.size()) {
            throw new BusinessException("VALIDATION_ERROR", "退款收费项不属于指定的原支付流水");
        }
        PaymentTransactionDraft transaction = new PaymentTransactionDraft(
                request.idempotencyKey(), original.registrationId(), "REFUND", original.paymentMethod(),
                total(items), operatorUserId, original.id(), request.reason().trim());
        mapper.insertTransaction(transaction);
        mapper.insertTransactionItems(transaction.getId(), items);
        if (mapper.markRefunded(ids) != ids.size()) {
            throw invalidState("收费项状态已发生变化，请刷新后重试");
        }
        items.forEach(item -> syncSource(item, false));
        return PaymentView.from(mapper.findTransactionById(transaction.getId()).orElseThrow());
    }

    private List<Long> distinctIds(List<Long> ids) {
        List<Long> distinct = ids.stream().distinct().toList();
        if (distinct.size() != ids.size()) {
            throw new BusinessException("VALIDATION_ERROR", "收费项不能重复选择");
        }
        return distinct;
    }

    private void requireChargeItems(
            List<ChargeItemRow> items, List<Long> ids, Long registrationId, String expectedState) {
        if (items.size() != ids.size()) {
            throw new BusinessException("RESOURCE_NOT_FOUND", "部分收费项不存在", HttpStatus.NOT_FOUND);
        }
        boolean invalid = items.stream().anyMatch(item -> !registrationId.equals(item.registrationId())
                || !expectedState.equals(item.state()));
        if (invalid) {
            throw invalidState("收费项不属于本次就诊或当前状态不允许操作");
        }
    }

    private BigDecimal total(List<ChargeItemRow> items) {
        return items.stream().map(ChargeItemRow::totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PaymentView requireSameRequest(PaymentTransactionRow row, String type,
                                           Long registrationId, Long originalTransactionId,
                                           String paymentMethod, String reason, List<Long> chargeItemIds) {
        boolean same = type.equals(row.transactionType()) && registrationId.equals(row.registrationId())
                && java.util.Objects.equals(originalTransactionId, row.originalTransactionId())
                && paymentMethod.equals(row.paymentMethod())
                && java.util.Objects.equals(reason, row.reason())
                && mapper.countAllTransactionItems(row.id()) == chargeItemIds.size()
                && mapper.countTransactionItems(row.id(), chargeItemIds) == chargeItemIds.size();
        if (!same) {
            throw new BusinessException("DUPLICATE_RESOURCE", "幂等键已被其他交易使用", HttpStatus.CONFLICT);
        }
        return PaymentView.from(row);
    }

    private void syncSource(ChargeItemRow item, boolean paying) {
        int changed = switch (item.itemType()) {
            case "CHECK" -> paying ? mapper.payCheck(item.sourceId()) : mapper.refundCheck(item.sourceId());
            case "INSPECTION" -> paying ? mapper.payInspection(item.sourceId()) : mapper.refundInspection(item.sourceId());
            case "DISPOSAL" -> paying ? mapper.payDisposal(item.sourceId()) : mapper.refundDisposal(item.sourceId());
            case "PRESCRIPTION" -> paying ? mapper.payPrescription(item.sourceId()) : mapper.refundPrescription(item.sourceId());
            case "REGISTRATION" -> 1;
            default -> throw new BusinessException("VALIDATION_ERROR", "不支持的收费项目类型");
        };
        if (changed != 1) {
            throw invalidState("关联业务单据状态不允许当前收费操作");
        }
    }

    private BusinessException invalidState(String message) {
        return new BusinessException("INVALID_STATE_TRANSITION", message, HttpStatus.CONFLICT);
    }
}
