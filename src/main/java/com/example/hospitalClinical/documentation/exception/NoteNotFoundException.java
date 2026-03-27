package com.example.hospitalClinical.documentation.exception;

import com.example.hospitalClinical.common.exception.BusinessException;
import com.example.hospitalClinical.common.exception.ErrorCode;

public class NoteNotFoundException extends BusinessException {

    public NoteNotFoundException() {
        super(ErrorCode.NOTE_NOT_FOUND);
    }
}
