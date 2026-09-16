package com.hospital.his.pharmacy.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.pharmacy.application.PharmacyConflictException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = PharmacyPrescriptionController.class)
public class PharmacyExceptionHandler {

    @ExceptionHandler(PharmacyConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(PharmacyConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getCode(), exception.getMessage()));
    }
}
