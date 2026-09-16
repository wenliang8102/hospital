package com.hospital.his.pharmacy.persistence.mapper;

import com.hospital.his.pharmacy.persistence.model.PrescriptionRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PrescriptionMapper {
    Optional<PrescriptionRow> findById(@Param("id") Long id);

    List<PrescriptionRow> findQueue(
            @Param("state") String state,
            @Param("keywordId") Long keywordId,
            @Param("offset") long offset,
            @Param("limit") int limit);

    long countQueue(@Param("state") String state, @Param("keywordId") Long keywordId);

    int markDispensed(@Param("id") Long id, @Param("dispensedBy") Long dispensedBy);

    int markReturned(@Param("id") Long id);
}
