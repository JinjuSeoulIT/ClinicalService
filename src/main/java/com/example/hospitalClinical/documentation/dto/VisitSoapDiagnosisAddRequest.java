package com.example.hospitalClinical.documentation.dto;

public class VisitSoapDiagnosisAddRequest {

    private String dxCode;
    private String dxName;
    private Boolean main;

    public String getDxCode() { return dxCode; }
    public void setDxCode(String dxCode) { this.dxCode = dxCode; }
    public String getDxName() { return dxName; }
    public void setDxName(String dxName) { this.dxName = dxName; }
    public Boolean getMain() { return main; }
    public void setMain(Boolean main) { this.main = main; }
}
