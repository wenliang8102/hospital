package com.hospital.his.platform.admin;

public class UserAccountDraft {
    private Long id;
    private final String username;
    private final String passwordHash;
    private final String displayName;
    private final Long employeeId;
    private final boolean enabled;

    public UserAccountDraft(String username, String passwordHash, String displayName, Long employeeId, boolean enabled) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.employeeId = employeeId;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
