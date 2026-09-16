package com.hospital.his.masterdata.persistence;

import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.masterdata.persistence.mapper.LookupMapper;
import com.hospital.his.masterdata.persistence.model.DictionaryRow;
import com.hospital.his.masterdata.persistence.model.DiseaseRow;
import com.hospital.his.masterdata.persistence.model.DrugRow;
import com.hospital.his.masterdata.persistence.model.EmployeeRow;
import com.hospital.his.masterdata.persistence.model.MedicalTechnologyRow;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LookupRepository {
    private final LookupMapper mapper;

    public LookupRepository(LookupMapper mapper) {
        this.mapper = mapper;
    }

    public List<EmployeeRow> findEmployees(String keyword, Long departmentId) {
        return mapper.findEmployees(keyword, departmentId);
    }

    public PageResult<DictionaryRow> findRegistLevels(String keyword, PageQuery page) {
        return toPage(mapper.countRegistLevels(keyword), page, mapper.findRegistLevels(keyword, page.offset(), page.size()));
    }

    public PageResult<DictionaryRow> findSettleCategories(String keyword, PageQuery page) {
        return toPage(mapper.countSettleCategories(keyword), page, mapper.findSettleCategories(keyword, page.offset(), page.size()));
    }

    public PageResult<DiseaseRow> findDiseases(String keyword, PageQuery page) {
        return toPage(mapper.countDiseases(keyword), page, mapper.findDiseases(keyword, page.offset(), page.size()));
    }

    public PageResult<DrugRow> findDrugs(String keyword, PageQuery page) {
        return toPage(mapper.countDrugs(keyword), page, mapper.findDrugs(keyword, page.offset(), page.size()));
    }

    public PageResult<MedicalTechnologyRow> findMedicalTechnologies(String keyword, PageQuery page) {
        return toPage(
                mapper.countMedicalTechnologies(keyword),
                page,
                mapper.findMedicalTechnologies(keyword, page.offset(), page.size()));
    }

    private <T> PageResult<T> toPage(long total, PageQuery page, List<T> items) {
        if (total == 0) {
            return PageResult.of(List.of(), page, 0);
        }
        return PageResult.of(items, page, total);
    }
}
