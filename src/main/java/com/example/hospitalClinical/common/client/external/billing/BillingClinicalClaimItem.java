package com.example.hospitalClinical.common.client.external.billing;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BillingClinicalClaimItem {

    public static final String SOURCE_TYPE_CLINICAL_ORDER_ITEM = "CLINICAL_ORDER_ITEM";

    private String itemName;
    private String itemCode;
    private String orderType;
    private Long sourceId;
    private String sourceType;
}
