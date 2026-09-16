package com.hospital.his.medicaltech.application;

public class MedicalTechConflictException extends RuntimeException {
    private final String code;

    private MedicalTechConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public static MedicalTechConflictException invalidStateTransition() {
        return new MedicalTechConflictException("INVALID_STATE_TRANSITION", "医技申请状态不允许当前操作");
    }

    public String getCode() {
        return code;
    }
}
