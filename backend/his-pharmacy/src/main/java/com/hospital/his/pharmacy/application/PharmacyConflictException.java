package com.hospital.his.pharmacy.application;

public class PharmacyConflictException extends RuntimeException {
    private final String code;

    private PharmacyConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public static PharmacyConflictException invalidStateTransition() {
        return new PharmacyConflictException("INVALID_STATE_TRANSITION", "处方状态不允许当前操作");
    }

    public static PharmacyConflictException insufficientStock() {
        return new PharmacyConflictException("INSUFFICIENT_STOCK", "药品库存不足");
    }

    public static PharmacyConflictException invalidStockOperation() {
        return new PharmacyConflictException("INVALID_STOCK_OPERATION", "库存操作不合法");
    }

    public String getCode() {
        return code;
    }
}
