package com.example.hospitalClinical.encounter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VitalAssessSaveHistoryLine {

    private String label;
    private String at;
    private String detail;
}
