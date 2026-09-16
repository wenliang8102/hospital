package com.hospital.his.masterdata.persistence.model;

public class EmployeeDraft {
    private Long id;
    private final String realName;
    private final Long departmentId;
    private final Long registLevelId;
    private final Long schedulingId;
    private final boolean active;

    public EmployeeDraft(String realName, Long departmentId, Long registLevelId, Long schedulingId, boolean active) {
        this.realName = realName;
        this.departmentId = departmentId;
        this.registLevelId = registLevelId;
        this.schedulingId = schedulingId;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRealName() {
        return realName;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public Long getRegistLevelId() {
        return registLevelId;
    }

    public Long getSchedulingId() {
        return schedulingId;
    }

    public boolean isActive() {
        return active;
    }
}
