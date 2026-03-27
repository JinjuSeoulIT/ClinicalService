package com.example.hospitalClinical.history.dto;

import com.example.hospitalClinical.history.entity.HistoryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HistoryUpdateRequest {

    private HistoryType historyType;
    private String name;
    private String memo;
}
