package com.example.hospitalClinical.order.exception;

import com.example.hospitalClinical.common.exception.BusinessException;
import com.example.hospitalClinical.common.exception.ErrorCode;

public class OrderNotFoundException extends BusinessException {

    public OrderNotFoundException() {
        super(ErrorCode.ORDER_NOT_FOUND);
    }
}
