package com.example.hospitalClinical.documentation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HiraProcedureItemDto {

    private String mdfeeCd;
    private String korNm;
    private String mdfeeDivNo;
}
