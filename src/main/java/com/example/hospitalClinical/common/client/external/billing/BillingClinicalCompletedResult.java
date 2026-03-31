package com.example.hospitalClinical.common.client.external.billing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillingClinicalCompletedResult {
    private Long billId;
    private boolean alreadyProcessed;
}
