package com.hospital.his.pharmacy.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.common.api.ModuleInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pharmacy")
public class PharmacyController {

    @GetMapping("/status")
    public ApiResponse<ModuleInfo> status() {
        return ApiResponse.success(ModuleInfo.ready("pharmacy", "药房管理"));
    }
}

