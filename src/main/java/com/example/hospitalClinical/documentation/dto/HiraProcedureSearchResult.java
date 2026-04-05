package com.example.hospitalClinical.documentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HiraProcedureSearchResult {

    private String resultCode;
    private String resultMsg;
    private int pageNo;
    private int numOfRows;
    private int totalCount;

    @Builder.Default
    private List<HiraProcedureItemDto> items = new ArrayList<>();
}
