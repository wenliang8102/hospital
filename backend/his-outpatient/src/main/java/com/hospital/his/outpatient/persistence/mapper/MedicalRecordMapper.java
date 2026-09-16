package com.hospital.his.outpatient.persistence.mapper;

import com.hospital.his.outpatient.persistence.model.MedicalRecordDraft;
import com.hospital.his.outpatient.persistence.model.MedicalRecordRow;
import com.hospital.his.outpatient.persistence.model.DiseaseRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MedicalRecordMapper {
    Optional<MedicalRecordRow> findByRegistrationId(Long registrationId);

    int insert(MedicalRecordDraft draft);

    int update(MedicalRecordDraft draft);

    List<DiseaseRow> findDiseases(Long medicalRecordId);

    long countActiveDiseases(@Param("ids") List<Long> ids);

    int deleteDiseases(Long medicalRecordId);

    int insertDiseases(@Param("medicalRecordId") Long medicalRecordId, @Param("diseaseIds") List<Long> diseaseIds);
}
