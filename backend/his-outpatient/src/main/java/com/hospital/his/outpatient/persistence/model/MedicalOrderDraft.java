package com.hospital.his.outpatient.persistence.model;

public class MedicalOrderDraft {
    private Long id;
    private final Long registrationId;
    private final Long medicalTechnologyId;
    private final String requestInfo;
    private final String bodyPosition;
    private final String remark;

    public MedicalOrderDraft(Long registrationId, Long medicalTechnologyId, String requestInfo,
                             String bodyPosition, String remark) {
        this.registrationId = registrationId;
        this.medicalTechnologyId = medicalTechnologyId;
        this.requestInfo = requestInfo;
        this.bodyPosition = bodyPosition;
        this.remark = remark;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRegistrationId() { return registrationId; }
    public Long getMedicalTechnologyId() { return medicalTechnologyId; }
    public String getRequestInfo() { return requestInfo; }
    public String getBodyPosition() { return bodyPosition; }
    public String getRemark() { return remark; }
}
