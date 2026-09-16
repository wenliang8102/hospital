package com.hospital.his.pharmacy.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.pharmacy.application.PharmacyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pharmacy/stocks")
public class DrugStockController {
    private final PharmacyService service;

    public DrugStockController(PharmacyService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResult<DrugStockResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer maxQuantity,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.success(service.searchStocks(keyword, maxQuantity, PageQuery.of(page, size)));
    }

    @PostMapping("/inbound")
    public ResponseEntity<Void> inbound(
            @Valid @RequestBody DrugStockInboundRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        service.inboundStock(request.drugId(), request.quantity(), longClaim(jwt, "uid"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/adjustment")
    public ResponseEntity<Void> adjust(
            @Valid @RequestBody DrugStockAdjustmentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        service.adjustStock(request.drugId(), request.targetQuantity(), longClaim(jwt, "uid"));
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
