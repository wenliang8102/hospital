package com.hospital.his.outpatient.persistence.model;

public class PrescriptionDraft {
    private Long id;
    private final Long registrationId;
    private final Long drugId;
    private final String drugUsage;
    private final int drugNumber;

    public PrescriptionDraft(Long registrationId, Long drugId, String drugUsage, int drugNumber) {
        this.registrationId = registrationId;
        this.drugId = drugId;
        this.drugUsage = drugUsage;
        this.drugNumber = drugNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRegistrationId() { return registrationId; }
    public Long getDrugId() { return drugId; }
    public String getDrugUsage() { return drugUsage; }
    public int getDrugNumber() { return drugNumber; }
}
