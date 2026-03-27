package com.example.hospitalClinical.order.dto;

import com.example.hospitalClinical.order.entity.OrderResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResultResponse {
    private Long resultId;
    private Long orderItemId;
    private String resultValue;
    private String resultStatus;
    private LocalDateTime resultDate;
    private LocalDateTime createdAt;

    public static OrderResultResponse from(OrderResult r) {
        return new OrderResultResponse(
                r.getResultId(), r.getOrderItemId(), r.getResultValue(), r.getResultStatus(),
                r.getResultDate(), r.getCreatedAt()
        );
    }
}
