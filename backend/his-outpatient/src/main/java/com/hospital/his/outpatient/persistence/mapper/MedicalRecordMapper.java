package com.hospital.his.outpatient.persistence.mapper;

import com.hospital.his.outpatient.persistence.model.MedicalRecordDraft;
import com.hospital.his.outpatient.persistence.model.MedicalRecordRow;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface MedicalRecordMapper {
    Optional<MedicalRecordRow> findByRegistrationId(Long registrationId);

    int insert(MedicalRecordDraft draft);

    int update(MedicalRecordDraft draft);
}
