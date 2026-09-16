package com.hospital.his.masterdata.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.common.api.ModuleInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/master-data")
public class MasterDataController {

    @GetMapping("/status")
    public ApiResponse<ModuleInfo> status() {
        return ApiResponse.success(ModuleInfo.ready("master-data", "基础数据"));
    }
}

