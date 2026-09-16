package com.hospital.his.masterdata.persistence.model;

import java.math.BigDecimal;

public class DrugDraft {
    private Long id;
    private final String code;
    private final String name;
    private final String format;
    private final String unit;
    private final String manufacturer;
    private final String dosage;
    private final String type;
    private final BigDecimal price;
    private final String mnemonicCode;
    private final boolean active;

    public DrugDraft(
            String code,
            String name,
            String format,
            String unit,
            String manufacturer,
            String dosage,
            String type,
            BigDecimal price,
            String mnemonicCode,
            boolean active) {
        this.code = code;
        this.name = name;
        this.format = format;
        this.unit = unit;
        this.manufacturer = manufacturer;
        this.dosage = dosage;
        this.type = type;
        this.price = price;
        this.mnemonicCode = mnemonicCode;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getFormat() { return format; }
    public String getUnit() { return unit; }
    public String getManufacturer() { return manufacturer; }
    public String getDosage() { return dosage; }
    public String getType() { return type; }
    public BigDecimal getPrice() { return price; }
    public String getMnemonicCode() { return mnemonicCode; }
    public boolean isActive() { return active; }
}
