package com.hospital.his.platform.admin;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platform")
@PreAuthorize("hasAuthority('platform:manage')")
public class PlatformAdminController {
    private final PlatformAdminService service;
    private final OperationLogService operationLogService;

    public PlatformAdminController(PlatformAdminService service, OperationLogService operationLogService) {
        this.service = service;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleView>> roles() {
        return ApiResponse.success(service.findRoles());
    }

    @GetMapping("/users")
    public ApiResponse<PageResult<UserAccountView>> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.success(service.searchUsers(keyword, enabled, PageQuery.of(page, size)));
    }

    @PostMapping("/users")
    public ApiResponse<UserAccountView> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(service.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<UserAccountView> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.success(service.updateUser(id, request));
    }

    @PatchMapping("/users/{id}/enabled")
    public ApiResponse<UserAccountView> changeEnabled(
            @PathVariable Long id,
            @Valid @RequestBody UserEnabledRequest request) {
        return ApiResponse.success(service.changeEnabled(id, request.enabled()));
    }

    @GetMapping("/operation-logs")
    public ApiResponse<PageResult<OperationLogRow>> operationLogs(
            @RequestParam(required = false) String moduleCode,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.success(operationLogService.search(moduleCode, action, keyword, PageQuery.of(page, size)));
    }
}
