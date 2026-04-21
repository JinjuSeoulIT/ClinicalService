package com.example.hospitalClinical.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // COMMON
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // CLINICAL / VISIT
    CLINICAL_NOT_FOUND(HttpStatus.NOT_FOUND, "진료(Clinical)를 찾을 수 없습니다."),
    VISIT_NOT_FOUND(HttpStatus.NOT_FOUND, "진료(Visit)를 찾을 수 없습니다."),
    INVALID_CLINICAL_STATUS(HttpStatus.BAD_REQUEST, "허용되지 않는 진료 상태 변경입니다."),
    CLINICAL_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    VISIT_ALREADY_EXISTS_FOR_RECEPTION(HttpStatus.BAD_REQUEST, "해당 접수로 이미 진행 중인 진료가 있습니다."),
    DOCTOR_VISIT_ALREADY_IN_PROGRESS(
            HttpStatus.BAD_REQUEST,
            "다른 환자의 진료가 진행 중입니다. 진료 완료 후 신규 진료를 시작해 주세요."),
    VISIT_VITALS_EDIT_FORBIDDEN(
            HttpStatus.BAD_REQUEST,
            "이 진료 방문에는 활력·문진을 저장할 수 없습니다. 새 접수 후 진료를 시작해 주세요."),

    // RECEPTION (접수 서비스 연동)
    RECEPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "접수를 찾을 수 없습니다."),
    RECEPTION_INVALID_STATUS(HttpStatus.BAD_REQUEST, "진료 시작이 불가한 접수 상태입니다. (대기/호출 상태만 가능)"),
    RECEPTION_API_ERROR(HttpStatus.BAD_GATEWAY, "접수 서비스 호출에 실패했습니다."),

    // PATIENT REF
    PATIENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "환자를 찾을 수 없습니다."),
    PATIENT_REF_API_ERROR(HttpStatus.BAD_GATEWAY, "환자 서비스 호출에 실패했습니다."),

    // NOTE (진료기록)
    NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "진료기록(Note)을 찾을 수 없습니다."),

    // ORDER
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "검사 오더를 찾을 수 없습니다."),
    ORDER_STATUS_CLINICIAN_FORBIDDEN(
            HttpStatus.BAD_REQUEST,
            "진료 화면에서는 오더 취소만 가능합니다. 진행·완료는 진료지원 시스템에서 반영됩니다."),
    ORDER_STATUS_NOT_CANCELLABLE(HttpStatus.BAD_REQUEST, "완료되었거나 이미 취소된 오더는 취소할 수 없습니다."),
    ORDER_STATUS_INVALID(HttpStatus.BAD_REQUEST, "허용되지 않는 오더 상태입니다.");

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