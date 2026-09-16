package com.hospital.his.masterdata.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.common.api.ModuleInfo;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.masterdata.persistence.model.DepartmentRow;
import com.hospital.his.masterdata.service.DepartmentService;
import com.hospital.his.masterdata.service.EmployeeManagementService;
import com.hospital.his.masterdata.service.LookupService;
import com.hospital.his.masterdata.service.MasterDataMaintenanceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/master-data")
public class MasterDataController {
    private final DepartmentService departmentService;
    private final EmployeeManagementService employeeManagementService;
    private final LookupService lookupService;
    private final MasterDataMaintenanceService maintenanceService;

    public MasterDataController(
            DepartmentService departmentService,
            EmployeeManagementService employeeManagementService,
            LookupService lookupService,
            MasterDataMaintenanceService maintenanceService) {
        this.departmentService = departmentService;
        this.employeeManagementService = employeeManagementService;
        this.lookupService = lookupService;
        this.maintenanceService = maintenanceService;
    }

    @GetMapping("/status")
    public ApiResponse<ModuleInfo> status() {
        return ApiResponse.success(ModuleInfo.ready("master-data", "基础数据"));
    }

    @GetMapping("/departments/manage")
    @PreAuthorize("hasAuthority('master-data:read')")
    public ApiResponse<PageResult<DepartmentRow>> departments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.success(departmentService.search(keyword, type, active, PageQuery.of(page, size)));
    }

    @PostMapping("/departments/manage")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<DepartmentRow> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.success(departmentService.create(request));
    }

    @PutMapping("/departments/manage/{id}")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<DepartmentRow> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.success(departmentService.update(id, request));
    }

    @PatchMapping("/departments/manage/{id}/active")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<DepartmentRow> changeDepartmentActive(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentActiveRequest request) {
        return ApiResponse.success(departmentService.changeActive(id, request.active()));
    }

    @GetMapping("/employees/manage")
    @PreAuthorize("hasAuthority('master-data:write') or hasAuthority('platform:manage')")
    public ApiResponse<PageResult<?>> managedEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.success(employeeManagementService.search(
                keyword,
                departmentId,
                active,
                PageQuery.of(page, size)));
    }

    @PostMapping("/employees/manage")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        return ApiResponse.success(employeeManagementService.create(request));
    }

    @PutMapping("/employees/manage/{id}")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return ApiResponse.success(employeeManagementService.update(id, request));
    }

    @PatchMapping("/employees/manage/{id}/active")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> changeEmployeeActive(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentActiveRequest request) {
        return ApiResponse.success(employeeManagementService.changeActive(id, request.active()));
    }

    @GetMapping("/diseases/manage")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> managedDiseases(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.success(maintenanceService.searchDiseases(keyword, active, PageQuery.of(page, size)));
    }

    @PostMapping("/diseases/manage")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> createDisease(@Valid @RequestBody DiseaseRequest request) {
        return ApiResponse.success(maintenanceService.createDisease(request));
    }

    @PutMapping("/diseases/manage/{id}")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> updateDisease(@PathVariable Long id, @Valid @RequestBody DiseaseRequest request) {
        return ApiResponse.success(maintenanceService.updateDisease(id, request));
    }

    @PatchMapping("/diseases/manage/{id}/active")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> changeDiseaseActive(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentActiveRequest request) {
        return ApiResponse.success(maintenanceService.changeDiseaseActive(id, request.active()));
    }

    @GetMapping("/drugs/manage")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> managedDrugs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.success(maintenanceService.searchDrugs(keyword, active, PageQuery.of(page, size)));
    }

    @PostMapping("/drugs/manage")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> createDrug(@Valid @RequestBody DrugRequest request) {
        return ApiResponse.success(maintenanceService.createDrug(request));
    }

    @PutMapping("/drugs/manage/{id}")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> updateDrug(@PathVariable Long id, @Valid @RequestBody DrugRequest request) {
        return ApiResponse.success(maintenanceService.updateDrug(id, request));
    }

    @PatchMapping("/drugs/manage/{id}/active")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> changeDrugActive(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentActiveRequest request) {
        return ApiResponse.success(maintenanceService.changeDrugActive(id, request.active()));
    }

    @GetMapping("/medical-technologies/manage")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> managedMedicalTechnologies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.success(maintenanceService.searchMedicalTechnologies(keyword, active, PageQuery.of(page, size)));
    }

    @PostMapping("/medical-technologies/manage")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> createMedicalTechnology(@Valid @RequestBody MedicalTechnologyRequest request) {
        return ApiResponse.success(maintenanceService.createMedicalTechnology(request));
    }

    @PutMapping("/medical-technologies/manage/{id}")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> updateMedicalTechnology(
            @PathVariable Long id,
            @Valid @RequestBody MedicalTechnologyRequest request) {
        return ApiResponse.success(maintenanceService.updateMedicalTechnology(id, request));
    }

    @PatchMapping("/medical-technologies/manage/{id}/active")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> changeMedicalTechnologyActive(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentActiveRequest request) {
        return ApiResponse.success(maintenanceService.changeMedicalTechnologyActive(id, request.active()));
    }

    @GetMapping("/scheduling/manage")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> managedScheduling(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.success(maintenanceService.searchScheduling(keyword, active, PageQuery.of(page, size)));
    }

    @PostMapping("/scheduling/manage")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> createScheduling(@Valid @RequestBody SchedulingRequest request) {
        return ApiResponse.success(maintenanceService.createScheduling(request));
    }

    @PutMapping("/scheduling/manage/{id}")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> updateScheduling(@PathVariable Long id, @Valid @RequestBody SchedulingRequest request) {
        return ApiResponse.success(maintenanceService.updateScheduling(id, request));
    }

    @PatchMapping("/scheduling/manage/{id}/active")
    @PreAuthorize("hasAuthority('master-data:write')")
    public ApiResponse<?> changeSchedulingActive(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentActiveRequest request) {
        return ApiResponse.success(maintenanceService.changeSchedulingActive(id, request.active()));
    }

    @GetMapping("/{resource}")
    @PreAuthorize("hasAuthority('master-data:read')")
    public ApiResponse<PageResult<?>> resource(
            @PathVariable String resource,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.success(lookupService.findResource(resource, keyword, PageQuery.of(page, size)));
    }
}
