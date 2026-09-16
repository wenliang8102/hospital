package com.hospital.his.outpatient.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MedicalRecordRequest(
        @Size(max = 500) String chiefComplaint,
        @Size(max = 10000) String presentIllness,
        @Size(max = 10000) String presentTreatment,
        @Size(max = 10000) String pastHistory,
        @Size(max = 10000) String allergyHistory,
        @Size(max = 10000) String physicalExamination,
        @Size(max = 10000) String examinationProposal,
        @Size(max = 10000) String precaution,
        @Size(max = 10000) String diagnosis,
        @Size(max = 10000) String treatmentPlan,
        @NotNull @Size(max = 20) List<Long> diseaseIds) {
}
