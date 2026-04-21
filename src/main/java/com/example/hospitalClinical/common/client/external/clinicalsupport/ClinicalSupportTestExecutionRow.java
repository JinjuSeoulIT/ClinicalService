package com.example.hospitalClinical.common.client.external.clinicalsupport;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClinicalSupportTestExecutionRow {

    @JsonAlias({"order_item_id", "ORDER_ITEM_ID"})
    private Long orderItemId;

    @JsonAlias({"progress_status", "PROGRESS_STATUS"})
    private String progressStatus;
}
