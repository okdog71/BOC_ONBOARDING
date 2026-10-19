package com.grovesoft.boctracker.common.web;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new ErrorResponse.FieldError(f.getField(), f.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST,
                new ErrorResponse("VALIDATION_ERROR", "입력값 검증에 실패했습니다.", fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException e) {
        List<ErrorResponse.FieldError> fieldErrors = e.getConstraintViolations().stream()
                .map(v -> new ErrorResponse.FieldError(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST,
                new ErrorResponse("VALIDATION_ERROR", "입력값 검증에 실패했습니다.", fieldErrors));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
        log.warn("Bad request: {}", e.getMessage());
        return build(HttpStatus.BAD_REQUEST, ErrorResponse.of("BAD_REQUEST", "요청 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler({NotFoundException.class, EntityNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException e) {
        return build(HttpStatus.NOT_FOUND, ErrorResponse.of("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRule(BusinessRuleException e) {
        return build(HttpStatus.BAD_REQUEST, ErrorResponse.of("BUSINESS_RULE", e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflictException(ConflictException e) {
        return build(HttpStatus.CONFLICT, ErrorResponse.of("CONFLICT", e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(DataIntegrityViolationException e) {
        log.warn("Data integrity violation: {}", e.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT,
                ErrorResponse.of("CONFLICT", "중복되었거나 제약조건을 위반하는 데이터입니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("Unhandled exception", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorResponse.of("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다."));
    }

    private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, ErrorResponse error) {
        return ResponseEntity.status(status).body(ApiResponse.fail(error));
    }
}
