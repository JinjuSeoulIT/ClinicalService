package com.example.hospitalClinical.common.exception;

import com.example.hospitalClinical.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ✅ 비즈니스 예외 처리
     */
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

    /**
     * ✅ 요청 파라미터 누락
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(
            MissingServletRequestParameterException e) {

        log.warn("[MissingParam] {}", e.getMessage());

        return ResponseEntity.badRequest().body(
                ApiResponse.fail("필수 요청 파라미터가 누락되었습니다: " + e.getParameterName())
        );
    }

    /**
     * ✅ DTO Validation 실패
     */
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

    /**
     * ✅ 정적 리소스 없음
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {

        log.debug("[NoResourceFound] {}", e.getMessage());

        return ResponseEntity.status(404).body(
                ApiResponse.fail("요청하신 리소스를 찾을 수 없습니다.")
        );
    }

    /**
     * ✅ 모든 예외 (진짜 서버 오류)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception e) {

        log.error("[UnhandledException]", e);

        return ResponseEntity.internalServerError().body(
                ApiResponse.fail("서버 오류가 발생했습니다.")
        );
    }
}
