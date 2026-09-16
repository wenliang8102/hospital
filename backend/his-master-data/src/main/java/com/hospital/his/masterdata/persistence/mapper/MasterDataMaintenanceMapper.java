package com.hospital.his.masterdata.persistence.mapper;

import com.hospital.his.masterdata.persistence.model.DiseaseDraft;
import com.hospital.his.masterdata.persistence.model.DiseaseRow;
import com.hospital.his.masterdata.persistence.model.DrugDraft;
import com.hospital.his.masterdata.persistence.model.DrugRow;
import com.hospital.his.masterdata.persistence.model.MedicalTechnologyDraft;
import com.hospital.his.masterdata.persistence.model.MedicalTechnologyRow;
import com.hospital.his.masterdata.persistence.model.SchedulingDraft;
import com.hospital.his.masterdata.persistence.model.SchedulingRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MasterDataMaintenanceMapper {
    Optional<DiseaseRow> findDiseaseById(Long id);

    List<DiseaseRow> findDiseases(
            @Param("keyword") String keyword,
            @Param("active") Boolean active,
            @Param("offset") long offset,
            @Param("limit") int limit);

    long countDiseases(@Param("keyword") String keyword, @Param("active") Boolean active);

    int insertDisease(DiseaseDraft draft);

    int updateDisease(@Param("id") Long id, @Param("draft") DiseaseDraft draft);

    int updateDiseaseActive(@Param("id") Long id, @Param("active") boolean active);

    Optional<DrugRow> findDrugById(Long id);

    List<DrugRow> findDrugs(
            @Param("keyword") String keyword,
            @Param("active") Boolean active,
            @Param("offset") long offset,
            @Param("limit") int limit);

    long countDrugs(@Param("keyword") String keyword, @Param("active") Boolean active);

    int insertDrug(DrugDraft draft);

    int updateDrug(@Param("id") Long id, @Param("draft") DrugDraft draft);

    int updateDrugActive(@Param("id") Long id, @Param("active") boolean active);

    Optional<MedicalTechnologyRow> findMedicalTechnologyById(Long id);

    List<MedicalTechnologyRow> findMedicalTechnologies(
            @Param("keyword") String keyword,
            @Param("active") Boolean active,
            @Param("offset") long offset,
            @Param("limit") int limit);

    long countMedicalTechnologies(@Param("keyword") String keyword, @Param("active") Boolean active);

    int insertMedicalTechnology(MedicalTechnologyDraft draft);

    int updateMedicalTechnology(@Param("id") Long id, @Param("draft") MedicalTechnologyDraft draft);

    int updateMedicalTechnologyActive(@Param("id") Long id, @Param("active") boolean active);

    Optional<SchedulingRow> findSchedulingById(Long id);

    List<SchedulingRow> findScheduling(
            @Param("keyword") String keyword,
            @Param("active") Boolean active,
            @Param("offset") long offset,
            @Param("limit") int limit);

    long countScheduling(@Param("keyword") String keyword, @Param("active") Boolean active);

    int insertScheduling(SchedulingDraft draft);

    int updateScheduling(@Param("id") Long id, @Param("draft") SchedulingDraft draft);

    int updateSchedulingActive(@Param("id") Long id, @Param("active") boolean active);
}
