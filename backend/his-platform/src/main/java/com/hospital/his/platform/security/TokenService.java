package com.hospital.his.platform.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {
    private final JwtEncoder encoder;
    private final String issuer;
    private final Duration ttl;

    public TokenService(JwtEncoder encoder,
                        @Value("${his.security.jwt.issuer}") String issuer,
                        @Value("${his.security.jwt.ttl}") Duration ttl) {
        this.encoder = encoder;
        this.issuer = issuer;
        this.ttl = ttl;
    }

    public LoginResponse issue(HospitalUserPrincipal principal) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(principal.username())
                .claim("uid", principal.id())
                .claim("displayName", principal.displayName())
                .claim("roles", principal.roles())
                .claim("permissions", principal.permissions());
        if (principal.employeeId() != null) {
            claims.claim("employeeId", principal.employeeId());
        }
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims.build())).getTokenValue();
        return new LoginResponse(token, "Bearer", ttl.toSeconds(), UserProfile.from(principal));
    }
}
