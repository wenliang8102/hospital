package com.hospital.his.pharmacy.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.pharmacy.application.PharmacyService;
import com.hospital.his.pharmacy.domain.PrescriptionState;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pharmacy/prescriptions")
@PreAuthorize("hasAuthority('pharmacy:write')")
public class PharmacyPrescriptionController {
    private final PharmacyService service;

    public PharmacyPrescriptionController(PharmacyService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResult<PharmacyPrescriptionResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PrescriptionState state,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.success(service.search(keyword, state, PageQuery.of(page, size)));
    }

    @PostMapping("/{prescriptionId}/dispense")
    public ResponseEntity<Void> dispense(@PathVariable Long prescriptionId, @AuthenticationPrincipal Jwt jwt) {
        service.dispense(prescriptionId, longClaim(jwt, "uid"), longClaim(jwt, "employeeId"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{prescriptionId}/return")
    public ResponseEntity<Void> returnPrescription(@PathVariable Long prescriptionId, @AuthenticationPrincipal Jwt jwt) {
        service.returnPrescription(prescriptionId, longClaim(jwt, "uid"));
        return ResponseEntity.noContent().build();
    }

    private Long longClaim(Jwt jwt, String claimName) {
        if (jwt == null) {
            return null;
        }
        Object value = jwt.getClaims().get(claimName);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.valueOf(text);
        }
        return null;
    }
}
