package com.example.hospitalClinical.documentation.dto;

import com.example.hospitalClinical.documentation.entity.Diagnosis;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisResponse {
    private Long diagnosisId;
    private Long noteId;
    private String patientCode;
    private String diagnosisCode;
    private String description;
    private LocalDateTime createdAt;

    public static DiagnosisResponse from(Diagnosis d) {
        return new DiagnosisResponse(
                d.getDiagnosisId(), d.getNoteId(), d.getPatientCode(), d.getDiagnosisCode(),
                d.getDescription(), d.getCreatedAt()
        );
    }
}
