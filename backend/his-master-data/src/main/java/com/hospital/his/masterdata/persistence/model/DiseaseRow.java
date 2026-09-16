package com.hospital.his.masterdata.persistence.model;

public record DiseaseRow(Long id, String code, String name, String icd, String category, boolean active) {
}
