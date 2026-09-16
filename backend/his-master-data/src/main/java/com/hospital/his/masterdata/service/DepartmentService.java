package com.hospital.his.masterdata.service;

import com.hospital.his.common.exception.BusinessException;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.masterdata.persistence.DepartmentRepository;
import com.hospital.his.masterdata.persistence.model.DepartmentDraft;
import com.hospital.his.masterdata.persistence.model.DepartmentRow;
import com.hospital.his.masterdata.web.DepartmentRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentService {
    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PageResult<DepartmentRow> search(String keyword, String type, Boolean active, PageQuery page) {
        return repository.search(keyword, type, active, page);
    }

    @Transactional
    public DepartmentRow create(DepartmentRequest request) {
        DepartmentDraft draft = toDraft(request);
        try {
            return repository.create(draft);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("DUPLICATE_RESOURCE", "科室编码已存在");
        }
    }

    @Transactional
    public DepartmentRow update(Long id, DepartmentRequest request) {
        try {
            DepartmentRow row = repository.update(id, toDraft(request));
            if (row == null) {
                throw new BusinessException("RESOURCE_NOT_FOUND", "科室不存在");
            }
            return row;
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("DUPLICATE_RESOURCE", "科室编码已存在");
        }
    }

    @Transactional
    public DepartmentRow changeActive(Long id, boolean active) {
        DepartmentRow row = repository.updateActive(id, active);
        if (row == null) {
            throw new BusinessException("RESOURCE_NOT_FOUND", "科室不存在");
        }
        return row;
    }

    private DepartmentDraft toDraft(DepartmentRequest request) {
        return new DepartmentDraft(
                request.code().trim(),
                request.name().trim(),
                request.type().trim(),
                request.active());
    }
}
