package com.hospital.his.registration.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.common.exception.BusinessException;
import com.hospital.his.registration.application.BillingService;
import com.hospital.his.registration.web.dto.ChargeItemView;
import com.hospital.his.registration.web.dto.PaymentRequest;
import com.hospital.his.registration.web.dto.PaymentView;
import com.hospital.his.registration.web.dto.RefundRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
@PreAuthorize("hasAuthority('registration:write')")
public class BillingController {
    private final BillingService service;

    public BillingController(BillingService service) {
        this.service = service;
    }

    @GetMapping("/registrations/{registrationId}/charge-items")
    public ApiResponse<List<ChargeItemView>> chargeItems(
            @PathVariable Long registrationId,
            @RequestParam(required = false)
            @Pattern(regexp = "UNPAID|PAID|REFUNDED|VOID") String state) {
        return ApiResponse.success(service.chargeItems(registrationId, state));
    }

    @PostMapping("/payments")
    public ApiResponse<PaymentView> pay(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PaymentRequest request) {
        return ApiResponse.success(service.pay(request, userId(jwt)));
    }

    @PostMapping("/refunds")
    public ApiResponse<PaymentView> refund(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RefundRequest request) {
        return ApiResponse.success(service.refund(request, userId(jwt)));
    }

    private Long userId(Jwt jwt) {
        Number userId = jwt.getClaim("uid");
        if (userId == null) {
            throw new BusinessException("ACCESS_DENIED", "当前令牌缺少操作员标识", HttpStatus.FORBIDDEN);
        }
        return userId.longValue();
    }
}
