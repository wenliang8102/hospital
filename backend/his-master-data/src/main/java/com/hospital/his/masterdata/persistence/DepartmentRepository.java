package com.hospital.his.masterdata.persistence;

import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.masterdata.persistence.mapper.DepartmentMapper;
import com.hospital.his.masterdata.persistence.model.DepartmentRow;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DepartmentRepository {
    private final DepartmentMapper mapper;

    public DepartmentRepository(DepartmentMapper mapper) {
        this.mapper = mapper;
    }

    public PageResult<DepartmentRow> search(String keyword, String type, Boolean active, PageQuery page) {
        long total = mapper.count(keyword, type, active);
        if (total == 0) {
            return PageResult.of(List.of(), page, 0);
        }
        return PageResult.of(
                mapper.findPage(keyword, type, active, page.offset(), page.size()),
                page,
                total);
    }
}
