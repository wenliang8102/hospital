package com.hospital.his.registration.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.common.api.ModuleInfo;
import com.hospital.his.registration.application.RegistrationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {
    private final RegistrationService service;

    public RegistrationController(RegistrationService service) {
        this.service = service;
    }

    @GetMapping("/status")
    public ApiResponse<ModuleInfo> status() {
        return ApiResponse.success(ModuleInfo.ready("registration", "挂号收费"));
    }

    @PostMapping("/case-numbers")
    @PreAuthorize("hasAuthority('registration:write')")
    public ApiResponse<String> generateCaseNumber() {
        return ApiResponse.success(service.generateCaseNumber());
    }
}
