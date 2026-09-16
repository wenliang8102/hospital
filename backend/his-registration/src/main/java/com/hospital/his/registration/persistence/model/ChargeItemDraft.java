package com.hospital.his.registration.persistence.model;

import java.math.BigDecimal;

public class ChargeItemDraft {
    private Long id;
    private final Long registrationId;
    private final String itemType;
    private final Long sourceId;
    private final String itemName;
    private final BigDecimal unitPrice;
    private final int quantity;
    private final BigDecimal totalAmount;

    public ChargeItemDraft(Long registrationId, String itemType, Long sourceId, String itemName,
                           BigDecimal unitPrice, int quantity, BigDecimal totalAmount) {
        this.registrationId = registrationId;
        this.itemType = itemType;
        this.sourceId = sourceId;
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRegistrationId() { return registrationId; }
    public String getItemType() { return itemType; }
    public Long getSourceId() { return sourceId; }
    public String getItemName() { return itemName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
