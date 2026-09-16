package com.hospital.his.masterdata.persistence.model;

import java.math.BigDecimal;

public class MedicalTechnologyDraft {
    private Long id;
    private final String code;
    private final String name;
    private final String format;
    private final BigDecimal price;
    private final String type;
    private final String priceType;
    private final Long departmentId;
    private final boolean active;

    public MedicalTechnologyDraft(
            String code,
            String name,
            String format,
            BigDecimal price,
            String type,
            String priceType,
            Long departmentId,
            boolean active) {
        this.code = code;
        this.name = name;
        this.format = format;
        this.price = price;
        this.type = type;
        this.priceType = priceType;
        this.departmentId = departmentId;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getFormat() { return format; }
    public BigDecimal getPrice() { return price; }
    public String getType() { return type; }
    public String getPriceType() { return priceType; }
    public Long getDepartmentId() { return departmentId; }
    public boolean isActive() { return active; }
}
