package com.hospital.his.masterdata.persistence.mapper;

import com.hospital.his.masterdata.persistence.model.DictionaryRow;
import com.hospital.his.masterdata.persistence.model.DiseaseRow;
import com.hospital.his.masterdata.persistence.model.DrugRow;
import com.hospital.his.masterdata.persistence.model.EmployeeRow;
import com.hospital.his.masterdata.persistence.model.MedicalTechnologyRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LookupMapper {
    List<EmployeeRow> findEmployees(@Param("keyword") String keyword, @Param("departmentId") Long departmentId);

    long countRegistLevels(@Param("keyword") String keyword);

    List<DictionaryRow> findRegistLevels(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("limit") int limit);

    long countSettleCategories(@Param("keyword") String keyword);

    List<DictionaryRow> findSettleCategories(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("limit") int limit);

    long countDiseases(@Param("keyword") String keyword);

    List<DiseaseRow> findDiseases(@Param("keyword") String keyword, @Param("offset") long offset, @Param("limit") int limit);

    long countDrugs(@Param("keyword") String keyword);

    List<DrugRow> findDrugs(@Param("keyword") String keyword, @Param("offset") long offset, @Param("limit") int limit);

    long countMedicalTechnologies(@Param("keyword") String keyword);

    List<MedicalTechnologyRow> findMedicalTechnologies(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("limit") int limit);
}
