package com.hospital.his.pharmacy.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.common.persistence.PageQuery;
import com.hospital.his.common.persistence.PageResult;
import com.hospital.his.pharmacy.application.PharmacyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/pharmacy/stock-transactions")
@PreAuthorize("hasAuthority('pharmacy:write')")
public class DrugStockTransactionController {
    private final PharmacyService service;

    public DrugStockTransactionController(PharmacyService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResult<DrugStockTransactionResponse>> search(
            @RequestParam(required = false) Long drugId,
            @RequestParam(required = false) Long prescriptionId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.success(service.searchStockTransactions(drugId, prescriptionId, PageQuery.of(page, size)));
    }
}
