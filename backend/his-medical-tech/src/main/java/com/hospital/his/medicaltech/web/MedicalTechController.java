package com.hospital.his.medicaltech.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.common.api.ModuleInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medical-tech")
public class MedicalTechController {

    @GetMapping("/status")
    public ApiResponse<ModuleInfo> status() {
        return ApiResponse.success(ModuleInfo.ready("medical-tech", "医技执行"));
    }
}

