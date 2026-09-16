package com.hospital.his.masterdata.persistence.model;

public class SchedulingDraft {
    private Long id;
    private final String name;
    private final String weekRule;
    private final boolean active;

    public SchedulingDraft(String name, String weekRule, boolean active) {
        this.name = name;
        this.weekRule = weekRule;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public String getWeekRule() { return weekRule; }
    public boolean isActive() { return active; }
}
