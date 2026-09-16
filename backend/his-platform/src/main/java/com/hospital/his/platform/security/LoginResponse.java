package com.hospital.his.platform.security;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserProfile user) {
}
