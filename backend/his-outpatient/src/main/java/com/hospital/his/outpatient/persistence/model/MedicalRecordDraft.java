package com.hospital.his.outpatient.persistence.model;

public record MedicalRecordDraft(
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
}
