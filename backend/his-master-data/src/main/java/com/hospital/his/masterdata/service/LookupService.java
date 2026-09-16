package com.hospital.his.masterdata.service;

import com.hospital.his.common.exception.BusinessException;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.masterdata.persistence.LookupRepository;
import com.hospital.his.masterdata.persistence.model.EmployeeRow;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LookupService {
    private final LookupRepository repository;

    public LookupService(LookupRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<EmployeeRow> findEmployees(String keyword, Long departmentId) {
        return repository.findEmployees(normalize(keyword), departmentId);
    }

    @Transactional(readOnly = true)
    public PageResult<?> findResource(String resource, String keyword, PageQuery page) {
        String normalizedKeyword = normalize(keyword);
        return switch (resource) {
            case "regist-levels" -> repository.findRegistLevels(normalizedKeyword, page);
            case "settle-categories" -> repository.findSettleCategories(normalizedKeyword, page);
            case "diseases" -> repository.findDiseases(normalizedKeyword, page);
            case "medical-technologies" -> repository.findMedicalTechnologies(normalizedKeyword, page);
            case "drugs" -> repository.findDrugs(normalizedKeyword, page);
            default -> throw new BusinessException(
                    "RESOURCE_NOT_FOUND", "基础数据资源不存在", HttpStatus.NOT_FOUND);
        };
    }

    private String normalize(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
