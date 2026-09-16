package com.hospital.his.masterdata.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.masterdata.persistence.mapper.RegistrationLookupMapper;
import com.hospital.his.masterdata.persistence.model.DepartmentOption;
import com.hospital.his.masterdata.persistence.model.DiseaseOption;
import com.hospital.his.masterdata.persistence.model.DrugOption;
import com.hospital.his.masterdata.persistence.model.EmployeeOption;
import com.hospital.his.masterdata.persistence.model.RegistrationLevelOption;
import com.hospital.his.masterdata.persistence.model.SettlementCategoryOption;
import com.hospital.his.masterdata.persistence.model.MedicalTechnologyOption;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/master-data")
@PreAuthorize("hasAnyAuthority('master-data:read', 'registration:write', 'outpatient:write')")
public class RegistrationLookupController {
    private final RegistrationLookupMapper mapper;

    public RegistrationLookupController(RegistrationLookupMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping("/departments")
    public ApiResponse<List<DepartmentOption>> departments(
            @RequestParam(required = false) @Size(max = 64) String keyword) {
        return ApiResponse.success(mapper.findDepartments(trimToNull(keyword)));
    }

    @GetMapping("/employees")
    public ApiResponse<List<EmployeeOption>> employees(
            @RequestParam(required = false) @Size(max = 64) String keyword,
            @RequestParam(required = false) Long departmentId) {
        return ApiResponse.success(mapper.findEmployees(trimToNull(keyword), departmentId));
    }

    @GetMapping("/regist-levels")
    public ApiResponse<List<RegistrationLevelOption>> registrationLevels() {
        return ApiResponse.success(mapper.findRegistrationLevels());
    }

    @GetMapping("/settle-categories")
    public ApiResponse<List<SettlementCategoryOption>> settlementCategories() {
        return ApiResponse.success(mapper.findSettlementCategories());
    }

    @GetMapping("/diseases")
    public ApiResponse<List<DiseaseOption>> diseases(
            @RequestParam(required = false) @Size(max = 64) String keyword) {
        return ApiResponse.success(mapper.findDiseases(trimToNull(keyword), 50));
    }

    @GetMapping("/medical-technologies")
    public ApiResponse<List<MedicalTechnologyOption>> medicalTechnologies(
            @RequestParam(required = false) @Size(max = 64) String keyword,
            @RequestParam(required = false) String type) {
        return ApiResponse.success(mapper.findMedicalTechnologies(trimToNull(keyword), trimToNull(type), 50));
    }

    @GetMapping("/drugs")
    public ApiResponse<List<DrugOption>> drugs(
            @RequestParam(required = false) @Size(max = 64) String keyword) {
        return ApiResponse.success(mapper.findDrugs(trimToNull(keyword), 50));
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
