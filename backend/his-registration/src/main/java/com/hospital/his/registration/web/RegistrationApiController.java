package com.hospital.his.registration.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.registration.application.RegistrationService;
import com.hospital.his.registration.web.dto.CreateRegistrationRequest;
import com.hospital.his.registration.web.dto.RegistrationView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/registrations")
@PreAuthorize("hasAuthority('registration:write')")
public class RegistrationApiController {
    private final RegistrationService service;

    public RegistrationApiController(RegistrationService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<RegistrationView> create(@Valid @RequestBody CreateRegistrationRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @GetMapping
    public ApiResponse<PageResult<RegistrationView>> search(
            @RequestParam(required = false) @Size(max = 64) String keyword,
            @RequestParam(required = false) @Pattern(regexp = "REGISTERED|IN_CONSULTATION|COMPLETED|CANCELLED") String state,
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
        return ApiResponse.success(service.search(keyword, state, PageQuery.of(page, size)));
    }

    @GetMapping("/{registrationId}")
    public ApiResponse<RegistrationView> get(@PathVariable Long registrationId) {
        return ApiResponse.success(service.get(registrationId));
    }

    @PostMapping("/{registrationId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long registrationId) {
        service.cancel(registrationId);
        return ResponseEntity.noContent().build();
    }
}
