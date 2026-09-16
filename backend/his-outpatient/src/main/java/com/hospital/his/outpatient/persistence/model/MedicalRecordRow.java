package com.hospital.his.outpatient.persistence.model;

import java.time.LocalDateTime;

public record MedicalRecordRow(
        Long id,
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
        String treatmentPlan,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
