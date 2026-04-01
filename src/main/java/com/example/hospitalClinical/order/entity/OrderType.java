package com.example.hospitalClinical.order.entity;

public enum OrderType {
    PRESCRIPTION,
    BLOOD,
    IMAGING,
    PROCEDURE,
    PATHOLOGY,
    SPECIMEN,
    ENDOSCOPY,
    PHYSIOLOGICAL,
    MEDICATION;

    public boolean isPrescription() {
        return this == PRESCRIPTION;
    }

    public boolean isLabCommittedType() {
        return switch (this) {
            case BLOOD, IMAGING, PATHOLOGY, ENDOSCOPY, PHYSIOLOGICAL, SPECIMEN-> true;
            default -> false;
        };
    }

    public boolean isTestCategory() {
        return switch (this) {
            case BLOOD, IMAGING, PATHOLOGY, SPECIMEN, ENDOSCOPY, PHYSIOLOGICAL -> true;
            default -> false;
        };
    }

    public boolean isTreatmentCategory() {
        return this == PROCEDURE || this == MEDICATION;
    }

    public boolean matchesApiOrderTypeFilter(String apiFilterUpper) {
        if (apiFilterUpper == null || apiFilterUpper.isBlank()) {
            return true;
        }
        return switch (apiFilterUpper) {
            case "PRESCRIPTION" -> isPrescription();
            case "TEST" -> isTestCategory();
            case "TREATMENT" -> isTreatmentCategory();
            default -> false;
        };
    }

    public static OrderType fromApi(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return OrderType.valueOf(raw.trim().toUpperCase());
    }
}
