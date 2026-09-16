package com.hospital.his.masterdata.persistence.model;

public class DepartmentDraft {
    private Long id;
    private final String code;
    private final String name;
    private final String type;
    private final boolean active;

    public DepartmentDraft(String code, String name, String type, boolean active) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isActive() { return active; }
}
