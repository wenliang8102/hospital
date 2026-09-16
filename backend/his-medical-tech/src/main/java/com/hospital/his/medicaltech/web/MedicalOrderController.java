package com.hospital.his.medicaltech.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.medicaltech.application.MedicalOrderService;
import com.hospital.his.medicaltech.domain.MedicalOrderState;
import com.hospital.his.medicaltech.domain.MedicalOrderType;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medical-orders")
public class MedicalOrderController {
    private final MedicalOrderService service;

    public MedicalOrderController(MedicalOrderService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResult<MedicalOrderResponse>> search(
            @RequestParam MedicalOrderType type,
            @RequestParam(required = false) MedicalOrderState state,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.success(service.search(type, state, keyword, PageQuery.of(page, size)));
    }

    @PostMapping("/{type}/{orderId}/accept")
    public ResponseEntity<Void> accept(
            @PathVariable MedicalOrderType type,
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt) {
        service.accept(type, orderId, longClaim(jwt, "employeeId"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{type}/{orderId}/execute")
    public ResponseEntity<Void> execute(@PathVariable MedicalOrderType type, @PathVariable Long orderId) {
        service.execute(type, orderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{type}/{orderId}/result")
    public ResponseEntity<Void> reportResult(
            @PathVariable MedicalOrderType type,
            @PathVariable Long orderId,
            @Valid @RequestBody MedicalOrderResultRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        service.reportResult(type, orderId, request, longClaim(jwt, "employeeId"));
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
