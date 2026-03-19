package com.example.hospitalClinical.common.exception;

import com.example.hospitalClinical.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();

        log.warn("[BusinessException] code={}, message={}",
                code.name(),
                code.getMessage());

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.fail(code.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(
            MissingServletRequestParameterException e) {

        log.warn("[MissingParam] {}", e.getMessage());

        return ResponseEntity.badRequest().body(
                ApiResponse.fail("필수 요청 파라미터가 누락되었습니다: " + e.getParameterName())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException e) {

        Map<String, String> errors = new HashMap<>();

        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }

        log.warn("[ValidationError] {}", errors);

        return ResponseEntity.badRequest().body(
                ApiResponse.fail("요청 값이 올바르지 않습니다.", errors)
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {

        log.debug("[NoResourceFound] {}", e.getMessage());

        return ResponseEntity.status(404).body(
                ApiResponse.fail("요청하신 리소스를 찾을 수 없습니다.")
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("[IllegalArgument] {}", e.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("[DataIntegrity] {}", e.getMessage());
        String msg = "저장 조건을 만족하지 않습니다. (예: receptionId에 해당하는 접수가 없을 수 있습니다.)";
        return ResponseEntity.badRequest().body(ApiResponse.fail(msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception e) {
        log.error("[UnhandledException] {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body(
                ApiResponse.fail("서버 오류가 발생했습니다. " + (e.getMessage() != null ? e.getMessage() : ""))
        );
    }
}