package com.example.hospitalClinical.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // COMMON
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // VISIT
    VISIT_NOT_FOUND(HttpStatus.NOT_FOUND, "내원(Visit)을 찾을 수 없습니다."),
    INVALID_VISIT_STATUS(HttpStatus.BAD_REQUEST, "허용되지 않는 진료 상태 변경입니다."),
    VISIT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // PATIENT REF
    PATIENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "환자를 찾을 수 없습니다."),
    PATIENT_REF_API_ERROR(HttpStatus.BAD_GATEWAY, "환자 서비스 호출에 실패했습니다."),

    // VISIT NOTE
    VISIT_NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "진료기록(VisitNote)을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}