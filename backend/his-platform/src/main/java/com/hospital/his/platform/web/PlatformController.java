package com.hospital.his.platform.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.common.api.ModuleInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform")
public class PlatformController {

    @GetMapping("/status")
    public ApiResponse<ModuleInfo> status() {
        return ApiResponse.success(ModuleInfo.ready("platform", "平台与权限"));
    }
}

