package com.hospital.his.outpatient.persistence.model;

public class MedicalRecordDraft {
    private Long id;
    private final Long registrationId;
    private final String chiefComplaint;
    private final String presentIllness;
    private final String presentTreatment;
    private final String pastHistory;
    private final String allergyHistory;
    private final String physicalExamination;
    private final String examinationProposal;
    private final String precaution;
    private final String diagnosis;
    private final String treatmentPlan;

    public MedicalRecordDraft(
        Long registrationId,
        String chiefComplaint,
        String presentIllness,
        String presentTreatment,
        String pastHistory,
        String allergyHistory,
        String physicalExamination,
        String examinationProposal,
        String precaution,
        String diagnosis,
        String treatmentPlan) {
        this.registrationId = registrationId;
        this.chiefComplaint = chiefComplaint;
        this.presentIllness = presentIllness;
        this.presentTreatment = presentTreatment;
        this.pastHistory = pastHistory;
        this.allergyHistory = allergyHistory;
        this.physicalExamination = physicalExamination;
        this.examinationProposal = examinationProposal;
        this.precaution = precaution;
        this.diagnosis = diagnosis;
        this.treatmentPlan = treatmentPlan;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRegistrationId() { return registrationId; }
    public String getChiefComplaint() { return chiefComplaint; }
    public String getPresentIllness() { return presentIllness; }
    public String getPresentTreatment() { return presentTreatment; }
    public String getPastHistory() { return pastHistory; }
    public String getAllergyHistory() { return allergyHistory; }
    public String getPhysicalExamination() { return physicalExamination; }
    public String getExaminationProposal() { return examinationProposal; }
    public String getPrecaution() { return precaution; }
    public String getDiagnosis() { return diagnosis; }
    public String getTreatmentPlan() { return treatmentPlan; }
}
