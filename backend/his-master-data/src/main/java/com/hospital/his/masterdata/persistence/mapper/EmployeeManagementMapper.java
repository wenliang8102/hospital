package com.hospital.his.masterdata.persistence.mapper;

import com.hospital.his.masterdata.persistence.model.EmployeeDraft;
import com.hospital.his.masterdata.persistence.model.EmployeeRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface EmployeeManagementMapper {
    Optional<EmployeeRow> findById(Long id);

    List<EmployeeRow> findPage(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("active") Boolean active,
            @Param("offset") long offset,
            @Param("limit") int limit);

    long count(@Param("keyword") String keyword, @Param("departmentId") Long departmentId, @Param("active") Boolean active);

    int insert(EmployeeDraft draft);

    int update(@Param("id") Long id, @Param("draft") EmployeeDraft draft);

    int updateActive(@Param("id") Long id, @Param("active") boolean active);
}
