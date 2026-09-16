package com.hospital.his.registration.persistence.model;

import java.math.BigDecimal;

public class PaymentTransactionDraft {
    private Long id;
    private final String transactionNo;
    private final Long registrationId;
    private final String transactionType;
    private final String paymentMethod;
    private final BigDecimal amount;
    private final Long operatorUserId;
    private final Long originalTransactionId;
    private final String reason;

    public PaymentTransactionDraft(String transactionNo, Long registrationId, String transactionType,
                                   String paymentMethod, BigDecimal amount, Long operatorUserId,
                                   Long originalTransactionId, String reason) {
        this.transactionNo = transactionNo;
        this.registrationId = registrationId;
        this.transactionType = transactionType;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.operatorUserId = operatorUserId;
        this.originalTransactionId = originalTransactionId;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTransactionNo() { return transactionNo; }
    public Long getRegistrationId() { return registrationId; }
    public String getTransactionType() { return transactionType; }
    public String getPaymentMethod() { return paymentMethod; }
    public BigDecimal getAmount() { return amount; }
    public Long getOperatorUserId() { return operatorUserId; }
    public Long getOriginalTransactionId() { return originalTransactionId; }
    public String getReason() { return reason; }
}
