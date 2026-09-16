package com.hospital.his.platform.security;

public class NewUser {
    private Long id;
    private final String username;
    private final String passwordHash;
    private final String displayName;

    public NewUser(String username, String passwordHash, String displayName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
}
