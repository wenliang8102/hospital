package com.hospital.his.outpatient.persistence.mapper;

import com.hospital.his.outpatient.persistence.model.DrugReference;
import com.hospital.his.outpatient.persistence.model.MedicalOrderDraft;
import com.hospital.his.outpatient.persistence.model.MedicalOrderRow;
import com.hospital.his.outpatient.persistence.model.MedicalTechnologyReference;
import com.hospital.his.outpatient.persistence.model.PrescriptionDraft;
import com.hospital.his.outpatient.persistence.model.PrescriptionRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ClinicalOrderMapper {
    Optional<MedicalTechnologyReference> findMedicalTechnology(Long id);
    Optional<DrugReference> findDrug(Long id);
    int insertCheck(MedicalOrderDraft draft);
    int insertInspection(MedicalOrderDraft draft);
    int insertDisposal(MedicalOrderDraft draft);
    int insertPrescription(PrescriptionDraft draft);
    List<MedicalOrderRow> findMedicalOrders(Long registrationId);
    List<PrescriptionRow> findPrescriptions(Long registrationId);
    int cancelCheck(@Param("id") Long id, @Param("registrationId") Long registrationId);
    int cancelInspection(@Param("id") Long id, @Param("registrationId") Long registrationId);
    int cancelDisposal(@Param("id") Long id, @Param("registrationId") Long registrationId);
    int cancelPrescription(@Param("id") Long id, @Param("registrationId") Long registrationId);
    int voidCharge(@Param("itemType") String itemType, @Param("sourceId") Long sourceId);
}
