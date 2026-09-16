package com.hospital.his.masterdata.persistence;

import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.masterdata.persistence.mapper.EmployeeManagementMapper;
import com.hospital.his.masterdata.persistence.model.EmployeeDraft;
import com.hospital.his.masterdata.persistence.model.EmployeeRow;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmployeeManagementRepository {
    private final EmployeeManagementMapper mapper;

    public EmployeeManagementRepository(EmployeeManagementMapper mapper) {
        this.mapper = mapper;
    }

    public PageResult<EmployeeRow> search(String keyword, Long departmentId, Boolean active, PageQuery page) {
        long total = mapper.count(keyword, departmentId, active);
        if (total == 0) {
            return PageResult.of(List.of(), page, 0);
        }
        return PageResult.of(mapper.findPage(keyword, departmentId, active, page.offset(), page.size()), page, total);
    }

    public EmployeeRow create(EmployeeDraft draft) {
        mapper.insert(draft);
        return mapper.findById(draft.getId()).orElseThrow();
    }

    public EmployeeRow update(Long id, EmployeeDraft draft) {
        if (mapper.update(id, draft) == 0) {
            return null;
        }
        return mapper.findById(id).orElseThrow();
    }

    public EmployeeRow updateActive(Long id, boolean active) {
        if (mapper.updateActive(id, active) == 0) {
            return null;
        }
        return mapper.findById(id).orElseThrow();
    }
}
