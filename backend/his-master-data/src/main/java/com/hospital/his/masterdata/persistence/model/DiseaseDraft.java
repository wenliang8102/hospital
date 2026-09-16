package com.hospital.his.masterdata.persistence.model;

public class DiseaseDraft {
    private Long id;
    private final String code;
    private final String name;
    private final String icd;
    private final String category;
    private final boolean active;

    public DiseaseDraft(String code, String name, String icd, String category, boolean active) {
        this.code = code;
        this.name = name;
        this.icd = icd;
        this.category = category;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getIcd() { return icd; }
    public String getCategory() { return category; }
    public boolean isActive() { return active; }
}
