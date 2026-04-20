package com.example.hospitalClinical.encounter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VitalAssessSaveHistoryLine {

    private String label;
    private LocalDateTime at;
}
