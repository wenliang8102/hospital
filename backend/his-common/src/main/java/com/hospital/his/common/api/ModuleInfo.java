package com.hospital.his.common.api;

public record ModuleInfo(String code, String name, String status) {

    public static ModuleInfo ready(String code, String name) {
        return new ModuleInfo(code, name, "READY");
    }
}

