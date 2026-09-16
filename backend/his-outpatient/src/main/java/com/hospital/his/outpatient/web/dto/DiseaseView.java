package com.hospital.his.outpatient.web.dto;

import com.hospital.his.outpatient.persistence.model.DiseaseRow;

public record DiseaseView(Long id, String code, String name, String icd) {
    public static DiseaseView from(DiseaseRow row) {
        return new DiseaseView(row.id(), row.code(), row.name(), row.icd());
    }
}
