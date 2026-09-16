package com.hospital.his.masterdata.persistence.mapper;

import com.hospital.his.masterdata.persistence.model.DepartmentDraft;
import com.hospital.his.masterdata.persistence.model.DepartmentRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DepartmentMapper {
    Optional<DepartmentRow> findById(Long id);

    List<DepartmentRow> findPage(
            @Param("keyword") String keyword,
            @Param("type") String type,
            @Param("active") Boolean active,
            @Param("offset") long offset,
            @Param("limit") int limit);

    long count(@Param("keyword") String keyword, @Param("type") String type, @Param("active") Boolean active);

    int insert(DepartmentDraft draft);

    int update(@Param("id") Long id, @Param("draft") DepartmentDraft draft);

    int updateActive(@Param("id") Long id, @Param("active") boolean active);
}
