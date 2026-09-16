package com.hospital.his.masterdata.service;

import com.hospital.his.common.exception.BusinessException;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.masterdata.persistence.EmployeeManagementRepository;
import com.hospital.his.masterdata.persistence.model.EmployeeDraft;
import com.hospital.his.masterdata.persistence.model.EmployeeRow;
import com.hospital.his.masterdata.web.EmployeeRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeManagementService {
    private final EmployeeManagementRepository repository;

    public EmployeeManagementService(EmployeeManagementRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PageResult<EmployeeRow> search(String keyword, Long departmentId, Boolean active, PageQuery page) {
        return repository.search(normalize(keyword), departmentId, active, page);
    }

    @Transactional
    public EmployeeRow create(EmployeeRequest request) {
        return repository.create(toDraft(request));
    }

    @Transactional
    public EmployeeRow update(Long id, EmployeeRequest request) {
        EmployeeRow row = repository.update(id, toDraft(request));
        if (row == null) {
            throw new BusinessException("RESOURCE_NOT_FOUND", "员工不存在");
        }
        return row;
    }

    @Transactional
    public EmployeeRow changeActive(Long id, boolean active) {
        EmployeeRow row = repository.updateActive(id, active);
        if (row == null) {
            throw new BusinessException("RESOURCE_NOT_FOUND", "员工不存在");
        }
        return row;
    }

    private EmployeeDraft toDraft(EmployeeRequest request) {
        return new EmployeeDraft(
                request.realName().trim(),
                request.departmentId(),
                request.registLevelId(),
                request.schedulingId(),
                request.active());
    }

    private String normalize(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
