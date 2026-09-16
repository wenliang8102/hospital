package com.hospital.his.platform.security;

import com.hospital.his.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfile> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(new UserProfile(
                jwt.getClaim("uid"),
                jwt.getSubject(),
                jwt.getClaimAsString("displayName"),
                jwt.getClaim("employeeId"),
                listClaim(jwt, "roles"),
                listClaim(jwt, "permissions")));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success(null);
    }

    private List<String> listClaim(Jwt jwt, String name) {
        List<String> values = jwt.getClaimAsStringList(name);
        return values == null ? List.of() : values;
    }
}
