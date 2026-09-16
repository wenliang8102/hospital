package com.hospital.his.platform.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OperationLogMapper {
    int insert(OperationLogDraft draft);

    long count(
            @Param("moduleCode") String moduleCode,
            @Param("action") String action,
            @Param("keyword") String keyword);

    List<OperationLogRow> findPage(
            @Param("moduleCode") String moduleCode,
            @Param("action") String action,
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("limit") int limit);
}
