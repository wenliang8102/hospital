package com.hospital.his.medicaltech.web;

import com.hospital.his.common.api.ApiResponse;
import com.hospital.his.medicaltech.application.MedicalTechConflictException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = MedicalOrderController.class)
public class MedicalTechExceptionHandler {

    @ExceptionHandler(MedicalTechConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(MedicalTechConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getCode(), exception.getMessage()));
    }
}
