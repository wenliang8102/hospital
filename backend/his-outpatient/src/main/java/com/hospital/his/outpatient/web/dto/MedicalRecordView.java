package com.hospital.his.outpatient.web.dto;

import com.hospital.his.outpatient.persistence.model.MedicalRecordRow;

import java.time.LocalDateTime;
import java.util.List;

public record MedicalRecordView(
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
        List<DiseaseView> diseases,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static MedicalRecordView from(MedicalRecordRow row, List<DiseaseView> diseases) {
        return new MedicalRecordView(row.id(), row.registrationId(), row.chiefComplaint(), row.presentIllness(),
                row.presentTreatment(), row.pastHistory(), row.allergyHistory(), row.physicalExamination(),
                row.examinationProposal(), row.precaution(), row.diagnosis(), row.treatmentPlan(),
                List.copyOf(diseases), row.createdAt(), row.updatedAt());
    }
}
