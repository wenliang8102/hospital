package com.hospital.his.masterdata.persistence.model;

public record SchedulingRow(Long id, String name, String weekRule, boolean active) {
}
