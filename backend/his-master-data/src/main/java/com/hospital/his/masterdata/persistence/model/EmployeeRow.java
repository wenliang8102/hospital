package com.hospital.his.masterdata.persistence.model;

public record EmployeeRow(
        Long id,
        String realName,
        Long departmentId,
        String departmentName,
        Long registLevelId,
        String registLevelName,
        Long schedulingId,
        String schedulingName,
        boolean active) {
}
