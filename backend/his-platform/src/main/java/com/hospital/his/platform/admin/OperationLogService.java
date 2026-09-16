package com.hospital.his.platform.admin;

import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OperationLogService {
    private final OperationLogMapper mapper;

    public OperationLogService(OperationLogMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResult<OperationLogRow> search(String moduleCode, String action, String keyword, PageQuery page) {
        String normalizedModuleCode = normalize(moduleCode);
        String normalizedAction = normalize(action);
        String normalizedKeyword = normalize(keyword);
        long total = mapper.count(normalizedModuleCode, normalizedAction, normalizedKeyword);
        if (total == 0) {
            return PageResult.of(List.of(), page, 0);
        }
        return PageResult.of(
                mapper.findPage(normalizedModuleCode, normalizedAction, normalizedKeyword, page.offset(), page.size()),
                page,
                total);
    }

    public void record(String moduleCode, String action, String targetType, String targetId, String detail) {
        mapper.insert(new OperationLogDraft(currentOperatorId(), moduleCode, action, targetType, targetId, detail));
    }

    private Long currentOperatorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return jwtAuthentication.getToken().getClaim("uid");
        }
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("uid");
        }
        return null;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
