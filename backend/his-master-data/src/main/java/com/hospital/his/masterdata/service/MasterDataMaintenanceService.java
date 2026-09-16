package com.hospital.his.masterdata.service;

import com.hospital.his.common.exception.BusinessException;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.masterdata.persistence.MasterDataMaintenanceRepository;
import com.hospital.his.masterdata.persistence.model.DiseaseDraft;
import com.hospital.his.masterdata.persistence.model.DiseaseRow;
import com.hospital.his.masterdata.persistence.model.DrugDraft;
import com.hospital.his.masterdata.persistence.model.DrugRow;
import com.hospital.his.masterdata.persistence.model.MedicalTechnologyDraft;
import com.hospital.his.masterdata.persistence.model.MedicalTechnologyRow;
import com.hospital.his.masterdata.persistence.model.SchedulingDraft;
import com.hospital.his.masterdata.persistence.model.SchedulingRow;
import com.hospital.his.masterdata.web.DiseaseRequest;
import com.hospital.his.masterdata.web.DrugRequest;
import com.hospital.his.masterdata.web.MedicalTechnologyRequest;
import com.hospital.his.masterdata.web.SchedulingRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MasterDataMaintenanceService {
    private final MasterDataMaintenanceRepository repository;

    public MasterDataMaintenanceService(MasterDataMaintenanceRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PageResult<DiseaseRow> searchDiseases(String keyword, Boolean active, PageQuery page) {
        return repository.searchDiseases(normalize(keyword), active, page);
    }

    @Transactional
    public DiseaseRow createDisease(DiseaseRequest request) {
        try {
            return repository.createDisease(toDiseaseDraft(request));
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("DUPLICATE_RESOURCE", "疾病编码已存在", HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public DiseaseRow updateDisease(Long id, DiseaseRequest request) {
        try {
            DiseaseRow row = repository.updateDisease(id, toDiseaseDraft(request));
            if (row == null) {
                throw new BusinessException("RESOURCE_NOT_FOUND", "疾病不存在", HttpStatus.NOT_FOUND);
            }
            return row;
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("DUPLICATE_RESOURCE", "疾病编码已存在", HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public DiseaseRow changeDiseaseActive(Long id, boolean active) {
        DiseaseRow row = repository.updateDiseaseActive(id, active);
        if (row == null) {
            throw new BusinessException("RESOURCE_NOT_FOUND", "疾病不存在", HttpStatus.NOT_FOUND);
        }
        return row;
    }

    @Transactional(readOnly = true)
    public PageResult<DrugRow> searchDrugs(String keyword, Boolean active, PageQuery page) {
        return repository.searchDrugs(normalize(keyword), active, page);
    }

    @Transactional
    public DrugRow createDrug(DrugRequest request) {
        try {
            return repository.createDrug(toDrugDraft(request));
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("DUPLICATE_RESOURCE", "药品编码已存在", HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public DrugRow updateDrug(Long id, DrugRequest request) {
        try {
            DrugRow row = repository.updateDrug(id, toDrugDraft(request));
            if (row == null) {
                throw new BusinessException("RESOURCE_NOT_FOUND", "药品不存在", HttpStatus.NOT_FOUND);
            }
            return row;
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("DUPLICATE_RESOURCE", "药品编码已存在", HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public DrugRow changeDrugActive(Long id, boolean active) {
        DrugRow row = repository.updateDrugActive(id, active);
        if (row == null) {
            throw new BusinessException("RESOURCE_NOT_FOUND", "药品不存在", HttpStatus.NOT_FOUND);
        }
        return row;
    }

    @Transactional(readOnly = true)
    public PageResult<MedicalTechnologyRow> searchMedicalTechnologies(String keyword, Boolean active, PageQuery page) {
        return repository.searchMedicalTechnologies(normalize(keyword), active, page);
    }

    @Transactional
    public MedicalTechnologyRow createMedicalTechnology(MedicalTechnologyRequest request) {
        try {
            return repository.createMedicalTechnology(toMedicalTechnologyDraft(request));
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("DUPLICATE_RESOURCE", "医技项目编码已存在", HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public MedicalTechnologyRow updateMedicalTechnology(Long id, MedicalTechnologyRequest request) {
        try {
            MedicalTechnologyRow row = repository.updateMedicalTechnology(id, toMedicalTechnologyDraft(request));
            if (row == null) {
                throw new BusinessException("RESOURCE_NOT_FOUND", "医技项目不存在", HttpStatus.NOT_FOUND);
            }
            return row;
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("DUPLICATE_RESOURCE", "医技项目编码已存在", HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public MedicalTechnologyRow changeMedicalTechnologyActive(Long id, boolean active) {
        MedicalTechnologyRow row = repository.updateMedicalTechnologyActive(id, active);
        if (row == null) {
            throw new BusinessException("RESOURCE_NOT_FOUND", "医技项目不存在", HttpStatus.NOT_FOUND);
        }
        return row;
    }

    @Transactional(readOnly = true)
    public PageResult<SchedulingRow> searchScheduling(String keyword, Boolean active, PageQuery page) {
        return repository.searchScheduling(normalize(keyword), active, page);
    }

    @Transactional
    public SchedulingRow createScheduling(SchedulingRequest request) {
        return repository.createScheduling(toSchedulingDraft(request));
    }

    @Transactional
    public SchedulingRow updateScheduling(Long id, SchedulingRequest request) {
        SchedulingRow row = repository.updateScheduling(id, toSchedulingDraft(request));
        if (row == null) {
            throw new BusinessException("RESOURCE_NOT_FOUND", "排班规则不存在", HttpStatus.NOT_FOUND);
        }
        return row;
    }

    @Transactional
    public SchedulingRow changeSchedulingActive(Long id, boolean active) {
        SchedulingRow row = repository.updateSchedulingActive(id, active);
        if (row == null) {
            throw new BusinessException("RESOURCE_NOT_FOUND", "排班规则不存在", HttpStatus.NOT_FOUND);
        }
        return row;
    }

    private DiseaseDraft toDiseaseDraft(DiseaseRequest request) {
        return new DiseaseDraft(
                request.code().trim(),
                request.name().trim(),
                normalize(request.icd()),
                normalize(request.category()),
                request.active());
    }

    private DrugDraft toDrugDraft(DrugRequest request) {
        return new DrugDraft(
                request.code().trim(),
                request.name().trim(),
                request.format().trim(),
                request.unit().trim(),
                normalize(request.manufacturer()),
                normalize(request.dosage()),
                normalize(request.type()),
                request.price(),
                normalize(request.mnemonicCode()),
                request.active());
    }

    private MedicalTechnologyDraft toMedicalTechnologyDraft(MedicalTechnologyRequest request) {
        return new MedicalTechnologyDraft(
                request.code().trim(),
                request.name().trim(),
                normalize(request.format()),
                request.price(),
                request.type().trim(),
                normalize(request.priceType()),
                request.departmentId(),
                request.active());
    }

    private SchedulingDraft toSchedulingDraft(SchedulingRequest request) {
        return new SchedulingDraft(
                request.name().trim(),
                request.weekRule().trim(),
                request.active());
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
