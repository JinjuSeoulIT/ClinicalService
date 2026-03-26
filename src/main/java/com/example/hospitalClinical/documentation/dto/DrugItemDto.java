package com.example.hospitalClinical.documentation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DrugItemDto {

    private String itemSeq;
    private String itemName;
    private String entpName;
    private String itemPermitDate;
    private String efcyQesitm;
    private String useMethodQesitm;
    private String atpnWarnQesitm;
    private String atpnQesitm;
    private String intrcQesitm;
    private String seQesitm;
    private String depositMethodQesitm;
    private String openDe;
    private String updateDe;
    private String itemImage;
}
