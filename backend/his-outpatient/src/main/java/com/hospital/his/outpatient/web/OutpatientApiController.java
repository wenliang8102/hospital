package com.hospital.his.outpatient.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.common.exception.BusinessException;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.outpatient.application.OutpatientService;
import com.hospital.his.outpatient.web.dto.MedicalRecordRequest;
import com.hospital.his.outpatient.web.dto.MedicalRecordView;
import com.hospital.his.outpatient.web.dto.PatientView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
@PreAuthorize("hasAuthority('outpatient:write')")
public class OutpatientApiController {
    private final OutpatientService service;

    public OutpatientApiController(OutpatientService service) {
        this.service = service;
    }

    @GetMapping("/outpatient/patients")
    public ApiResponse<PageResult<PatientView>> patients(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @Size(max = 64) String keyword,
            @RequestParam(required = false)
            @Pattern(regexp = "REGISTERED|IN_CONSULTATION|COMPLETED") String state,
            @RequestParam(required = false) @Min(1) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
        return ApiResponse.success(service.searchPatients(
                employeeId(jwt), keyword, state, PageQuery.of(page, size)));
    }

    @PostMapping("/registrations/{registrationId}/accept")
    public ApiResponse<PatientView> accept(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long registrationId) {
        return ApiResponse.success(service.accept(registrationId, employeeId(jwt)));
    }

    @GetMapping("/registrations/{registrationId}/medical-record")
    public ApiResponse<MedicalRecordView> getMedicalRecord(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long registrationId) {
        return ApiResponse.success(service.getMedicalRecord(registrationId, employeeId(jwt)));
    }

    @PutMapping("/registrations/{registrationId}/medical-record")
    public ApiResponse<MedicalRecordView> saveMedicalRecord(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long registrationId,
            @Valid @RequestBody MedicalRecordRequest request) {
        return ApiResponse.success(service.saveMedicalRecord(registrationId, employeeId(jwt), request));
    }

    @PostMapping("/registrations/{registrationId}/complete")
    public ApiResponse<PatientView> complete(
            @AuthenticationPrincipal Jwt jwt, @PathVariable Long registrationId) {
        return ApiResponse.success(service.complete(registrationId, employeeId(jwt)));
    }

    private Long employeeId(Jwt jwt) {
        Number employeeId = jwt.getClaim("employeeId");
        if (employeeId == null) {
            throw new BusinessException("ACCESS_DENIED", "当前账号未绑定医生档案", HttpStatus.FORBIDDEN);
        }
        return employeeId.longValue();
    }
}
