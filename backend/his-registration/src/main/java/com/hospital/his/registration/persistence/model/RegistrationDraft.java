package com.hospital.his.registration.persistence.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RegistrationDraft {
    private Long id;
    private final String requestNo;
    private final String caseNumber;
    private final String realName;
    private final String gender;
    private final String cardNumber;
    private final LocalDate birthday;
    private final Integer age;
    private final String ageType;
    private final String homeAddress;
    private final LocalDateTime visitDate;
    private final String noon;
    private final Long departmentId;
    private final Long employeeId;
    private final Long registrationLevelId;
    private final Long settlementCategoryId;
    private final boolean booked;
    private final String registrationMethod;
    private final BigDecimal registrationFee;

    public RegistrationDraft(String requestNo, String caseNumber, String realName, String gender,
            String cardNumber, LocalDate birthday, Integer age, String ageType, String homeAddress,
            LocalDateTime visitDate, String noon, Long departmentId, Long employeeId,
            Long registrationLevelId, Long settlementCategoryId, boolean booked,
            String registrationMethod, BigDecimal registrationFee) {
        this.requestNo = requestNo;
        this.caseNumber = caseNumber;
        this.realName = realName;
        this.gender = gender;
        this.cardNumber = cardNumber;
        this.birthday = birthday;
        this.age = age;
        this.ageType = ageType;
        this.homeAddress = homeAddress;
        this.visitDate = visitDate;
        this.noon = noon;
        this.departmentId = departmentId;
        this.employeeId = employeeId;
        this.registrationLevelId = registrationLevelId;
        this.settlementCategoryId = settlementCategoryId;
        this.booked = booked;
        this.registrationMethod = registrationMethod;
        this.registrationFee = registrationFee;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestNo() { return requestNo; }
    public String getCaseNumber() { return caseNumber; }
    public String getRealName() { return realName; }
    public String getGender() { return gender; }
    public String getCardNumber() { return cardNumber; }
    public LocalDate getBirthday() { return birthday; }
    public Integer getAge() { return age; }
    public String getAgeType() { return ageType; }
    public String getHomeAddress() { return homeAddress; }
    public LocalDateTime getVisitDate() { return visitDate; }
    public String getNoon() { return noon; }
    public Long getDepartmentId() { return departmentId; }
    public Long getEmployeeId() { return employeeId; }
    public Long getRegistrationLevelId() { return registrationLevelId; }
    public Long getSettlementCategoryId() { return settlementCategoryId; }
    public boolean isBooked() { return booked; }
    public String getRegistrationMethod() { return registrationMethod; }
    public BigDecimal getRegistrationFee() { return registrationFee; }
}
