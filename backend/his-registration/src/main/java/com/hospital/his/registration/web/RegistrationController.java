package com.hospital.his.registration.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.common.api.ModuleInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    @GetMapping("/status")
    public ApiResponse<ModuleInfo> status() {
        return ApiResponse.success(ModuleInfo.ready("registration", "挂号收费"));
    }
}

