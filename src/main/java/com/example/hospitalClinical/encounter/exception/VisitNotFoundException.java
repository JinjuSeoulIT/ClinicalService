package com.example.hospitalClinical.encounter.exception;

import com.example.hospitalClinical.common.exception.BusinessException;
import com.example.hospitalClinical.common.exception.ErrorCode;

public class VisitNotFoundException extends BusinessException {

    public VisitNotFoundException() {
        super(ErrorCode.VISIT_NOT_FOUND);
    }
}
