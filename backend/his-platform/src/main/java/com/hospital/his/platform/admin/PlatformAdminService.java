package com.hospital.his.platform.admin;

import com.hospital.his.common.exception.BusinessException;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlatformAdminService {
    private final PlatformAdminMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;

    public PlatformAdminService(
            PlatformAdminMapper mapper,
            PasswordEncoder passwordEncoder,
            OperationLogService operationLogService) {
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.operationLogService = operationLogService;
    }

    @Transactional(readOnly = true)
    public List<RoleView> findRoles() {
        return mapper.findRoles();
    }

    @Transactional(readOnly = true)
    public PageResult<UserAccountView> searchUsers(String keyword, Boolean enabled, PageQuery page) {
        String normalizedKeyword = normalize(keyword);
        long total = mapper.countUsers(normalizedKeyword, enabled);
        if (total == 0) {
            return PageResult.of(List.of(), page, 0);
        }
        List<UserAccountRow> rows = mapper.findUsers(normalizedKeyword, enabled, page.offset(), page.size());
        return PageResult.of(hydrateRoles(rows), page, total);
    }

    @Transactional
    public UserAccountView createUser(CreateUserRequest request) {
        UserAccountDraft draft = new UserAccountDraft(
                request.username().trim(),
                passwordEncoder.encode(request.password()),
                request.displayName().trim(),
                request.employeeId(),
                request.enabled());
        try {
            mapper.insertUser(draft);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("DUPLICATE_RESOURCE", "用户名已存在");
        }
        replaceRoles(draft.getId(), request.roleCodes());
        UserAccountView user = findUserOrThrow(draft.getId());
        operationLogService.record("platform", "USER_CREATE", "USER", user.id().toString(), "创建账号 " + user.username());
        return user;
    }

    @Transactional
    public UserAccountView updateUser(Long id, UpdateUserRequest request) {
        int updated = mapper.updateUser(id, request.displayName().trim(), request.employeeId(), request.enabled());
        if (updated == 0) {
            throw new BusinessException("RESOURCE_NOT_FOUND", "账号不存在");
        }
        replaceRoles(id, request.roleCodes());
        UserAccountView user = findUserOrThrow(id);
        operationLogService.record("platform", "USER_UPDATE", "USER", user.id().toString(), "更新账号 " + user.username());
        return user;
    }

    @Transactional
    public UserAccountView changeEnabled(Long id, boolean enabled) {
        if (mapper.updateEnabled(id, enabled) == 0) {
            throw new BusinessException("RESOURCE_NOT_FOUND", "账号不存在");
        }
        UserAccountView user = findUserOrThrow(id);
        operationLogService.record(
                "platform",
                enabled ? "USER_ENABLE" : "USER_DISABLE",
                "USER",
                user.id().toString(),
                (enabled ? "启用账号 " : "停用账号 ") + user.username());
        return user;
    }

    private UserAccountView findUserOrThrow(Long id) {
        UserAccountRow row = mapper.findUserById(id)
                .orElseThrow(() -> new BusinessException("RESOURCE_NOT_FOUND", "账号不存在"));
        return hydrateRoles(List.of(row)).getFirst();
    }

    private void replaceRoles(Long userId, List<String> roleCodes) {
        List<Long> roleIds = mapper.findRoleIdsByCodes(roleCodes);
        if (roleIds.size() != roleCodes.size()) {
            throw new BusinessException("RESOURCE_NOT_FOUND", "角色不存在");
        }
        mapper.deleteUserRoles(userId);
        for (Long roleId : roleIds) {
            mapper.insertUserRole(userId, roleId);
        }
    }

    private List<UserAccountView> hydrateRoles(List<UserAccountRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = rows.stream().map(UserAccountRow::id).toList();
        Map<Long, List<RoleView>> rolesByUser = new LinkedHashMap<>();
        for (RoleAssignmentRow assignment : mapper.findRolesByUserIds(userIds)) {
            rolesByUser.computeIfAbsent(assignment.userId(), ignored -> new java.util.ArrayList<>())
                    .add(new RoleView(assignment.roleCode(), assignment.roleName()));
        }
        return rows.stream()
                .map(row -> new UserAccountView(
                        row.id(),
                        row.username(),
                        row.displayName(),
                        row.employeeId(),
                        row.employeeName(),
                        row.enabled(),
                        rolesByUser.getOrDefault(row.id(), List.of())))
                .toList();
    }

    private String normalize(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
