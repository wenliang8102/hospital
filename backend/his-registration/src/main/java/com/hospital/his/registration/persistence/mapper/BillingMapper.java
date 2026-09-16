package com.hospital.his.registration.persistence.mapper;

import com.hospital.his.registration.persistence.model.ChargeItemRow;
import com.hospital.his.registration.persistence.model.PaymentTransactionDraft;
import com.hospital.his.registration.persistence.model.PaymentTransactionRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BillingMapper {
    List<ChargeItemRow> findChargeItems(@Param("registrationId") Long registrationId,
                                        @Param("state") String state);
    List<ChargeItemRow> lockChargeItems(@Param("ids") List<Long> ids);
    Optional<PaymentTransactionRow> findTransactionByNo(String transactionNo);
    Optional<PaymentTransactionRow> findTransactionById(Long id);
    int insertTransaction(PaymentTransactionDraft draft);
    int insertTransactionItems(@Param("transactionId") Long transactionId,
                               @Param("items") List<ChargeItemRow> items);
    long countTransactionItems(@Param("transactionId") Long transactionId,
                               @Param("chargeItemIds") List<Long> chargeItemIds);
    long countAllTransactionItems(Long transactionId);
    int markPaid(@Param("ids") List<Long> ids);
    int markRefunded(@Param("ids") List<Long> ids);
    int payCheck(Long id);
    int payInspection(Long id);
    int payDisposal(Long id);
    int payPrescription(Long id);
    int refundCheck(Long id);
    int refundInspection(Long id);
    int refundDisposal(Long id);
    int refundPrescription(Long id);
}
