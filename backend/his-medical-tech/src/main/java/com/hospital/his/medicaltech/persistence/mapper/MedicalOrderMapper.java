package com.hospital.his.medicaltech.persistence.mapper;

import com.hospital.his.medicaltech.persistence.model.MedicalOrderRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MedicalOrderMapper {
    Optional<MedicalOrderRow> findCheckById(@Param("id") Long id);

    Optional<MedicalOrderRow> findInspectionById(@Param("id") Long id);

    Optional<MedicalOrderRow> findDisposalById(@Param("id") Long id);

    List<MedicalOrderRow> findCheckOrders(
            @Param("state") String state,
            @Param("keywordId") Long keywordId,
            @Param("offset") long offset,
            @Param("limit") int limit);

    List<MedicalOrderRow> findInspectionOrders(
            @Param("state") String state,
            @Param("keywordId") Long keywordId,
            @Param("offset") long offset,
            @Param("limit") int limit);

    List<MedicalOrderRow> findDisposalOrders(
            @Param("state") String state,
            @Param("keywordId") Long keywordId,
            @Param("offset") long offset,
            @Param("limit") int limit);

    long countCheckOrders(@Param("state") String state, @Param("keywordId") Long keywordId);

    long countInspectionOrders(@Param("state") String state, @Param("keywordId") Long keywordId);

    long countDisposalOrders(@Param("state") String state, @Param("keywordId") Long keywordId);

    int acceptCheck(@Param("id") Long id, @Param("executorEmployeeId") Long executorEmployeeId);

    int acceptInspection(@Param("id") Long id, @Param("executorEmployeeId") Long executorEmployeeId);

    int acceptDisposal(@Param("id") Long id, @Param("executorEmployeeId") Long executorEmployeeId);

    int executeCheck(@Param("id") Long id);

    int executeInspection(@Param("id") Long id);

    int executeDisposal(@Param("id") Long id);

    int reportCheckResult(
            @Param("id") Long id,
            @Param("resultEmployeeId") Long resultEmployeeId,
            @Param("result") String result,
            @Param("remark") String remark);

    int reportInspectionResult(
            @Param("id") Long id,
            @Param("resultEmployeeId") Long resultEmployeeId,
            @Param("result") String result,
            @Param("remark") String remark);

    int reportDisposalResult(
            @Param("id") Long id,
            @Param("resultEmployeeId") Long resultEmployeeId,
            @Param("result") String result,
            @Param("remark") String remark);
}
