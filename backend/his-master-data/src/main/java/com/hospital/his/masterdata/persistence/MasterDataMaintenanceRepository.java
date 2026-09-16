package com.hospital.his.masterdata.persistence;

import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.masterdata.persistence.mapper.MasterDataMaintenanceMapper;
import com.hospital.his.masterdata.persistence.model.DiseaseDraft;
import com.hospital.his.masterdata.persistence.model.DiseaseRow;
import com.hospital.his.masterdata.persistence.model.DrugDraft;
import com.hospital.his.masterdata.persistence.model.DrugRow;
import com.hospital.his.masterdata.persistence.model.MedicalTechnologyDraft;
import com.hospital.his.masterdata.persistence.model.MedicalTechnologyRow;
import com.hospital.his.masterdata.persistence.model.SchedulingDraft;
import com.hospital.his.masterdata.persistence.model.SchedulingRow;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MasterDataMaintenanceRepository {
    private final MasterDataMaintenanceMapper mapper;

    public MasterDataMaintenanceRepository(MasterDataMaintenanceMapper mapper) {
        this.mapper = mapper;
    }

    public PageResult<DiseaseRow> searchDiseases(String keyword, Boolean active, PageQuery page) {
        long total = mapper.countDiseases(keyword, active);
        if (total == 0) {
            return PageResult.of(List.of(), page, 0);
        }
        return PageResult.of(mapper.findDiseases(keyword, active, page.offset(), page.size()), page, total);
    }

    public DiseaseRow createDisease(DiseaseDraft draft) {
        mapper.insertDisease(draft);
        return mapper.findDiseaseById(draft.getId()).orElseThrow();
    }

    public DiseaseRow updateDisease(Long id, DiseaseDraft draft) {
        if (mapper.updateDisease(id, draft) == 0) {
            return null;
        }
        return mapper.findDiseaseById(id).orElseThrow();
    }

    public DiseaseRow updateDiseaseActive(Long id, boolean active) {
        if (mapper.updateDiseaseActive(id, active) == 0) {
            return null;
        }
        return mapper.findDiseaseById(id).orElseThrow();
    }

    public PageResult<DrugRow> searchDrugs(String keyword, Boolean active, PageQuery page) {
        long total = mapper.countDrugs(keyword, active);
        if (total == 0) {
            return PageResult.of(List.of(), page, 0);
        }
        return PageResult.of(mapper.findDrugs(keyword, active, page.offset(), page.size()), page, total);
    }

    public DrugRow createDrug(DrugDraft draft) {
        mapper.insertDrug(draft);
        return mapper.findDrugById(draft.getId()).orElseThrow();
    }

    public DrugRow updateDrug(Long id, DrugDraft draft) {
        if (mapper.updateDrug(id, draft) == 0) {
            return null;
        }
        return mapper.findDrugById(id).orElseThrow();
    }

    public DrugRow updateDrugActive(Long id, boolean active) {
        if (mapper.updateDrugActive(id, active) == 0) {
            return null;
        }
        return mapper.findDrugById(id).orElseThrow();
    }

    public PageResult<MedicalTechnologyRow> searchMedicalTechnologies(String keyword, Boolean active, PageQuery page) {
        long total = mapper.countMedicalTechnologies(keyword, active);
        if (total == 0) {
            return PageResult.of(List.of(), page, 0);
        }
        return PageResult.of(mapper.findMedicalTechnologies(keyword, active, page.offset(), page.size()), page, total);
    }

    public MedicalTechnologyRow createMedicalTechnology(MedicalTechnologyDraft draft) {
        mapper.insertMedicalTechnology(draft);
        return mapper.findMedicalTechnologyById(draft.getId()).orElseThrow();
    }

    public MedicalTechnologyRow updateMedicalTechnology(Long id, MedicalTechnologyDraft draft) {
        if (mapper.updateMedicalTechnology(id, draft) == 0) {
            return null;
        }
        return mapper.findMedicalTechnologyById(id).orElseThrow();
    }

    public MedicalTechnologyRow updateMedicalTechnologyActive(Long id, boolean active) {
        if (mapper.updateMedicalTechnologyActive(id, active) == 0) {
            return null;
        }
        return mapper.findMedicalTechnologyById(id).orElseThrow();
    }

    public PageResult<SchedulingRow> searchScheduling(String keyword, Boolean active, PageQuery page) {
        long total = mapper.countScheduling(keyword, active);
        if (total == 0) {
            return PageResult.of(List.of(), page, 0);
        }
        return PageResult.of(mapper.findScheduling(keyword, active, page.offset(), page.size()), page, total);
    }

    public SchedulingRow createScheduling(SchedulingDraft draft) {
        mapper.insertScheduling(draft);
        return mapper.findSchedulingById(draft.getId()).orElseThrow();
    }

    public SchedulingRow updateScheduling(Long id, SchedulingDraft draft) {
        if (mapper.updateScheduling(id, draft) == 0) {
            return null;
        }
        return mapper.findSchedulingById(id).orElseThrow();
    }

    public SchedulingRow updateSchedulingActive(Long id, boolean active) {
        if (mapper.updateSchedulingActive(id, active) == 0) {
            return null;
        }
        return mapper.findSchedulingById(id).orElseThrow();
    }
}
