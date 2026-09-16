package com.hospital.his.medicaltech.persistence.mapper;

import com.hospital.his.medicaltech.persistence.model.CheckRequestRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CheckRequestMapper {
    Optional<CheckRequestRow> findById(Long id);

    List<CheckRequestRow> findQueue(@Param("state") String state, @Param("offset") long offset, @Param("limit") int limit);

    int accept(@Param("id") Long id, @Param("executorEmployeeId") Long executorEmployeeId);

    int markExecuted(@Param("id") Long id);

    int reportResult(
            @Param("id") Long id,
            @Param("resultEmployeeId") Long resultEmployeeId,
            @Param("result") String result,
            @Param("remark") String remark);
}
