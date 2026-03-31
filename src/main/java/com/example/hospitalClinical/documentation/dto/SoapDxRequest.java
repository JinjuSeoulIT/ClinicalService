package com.example.hospitalClinical.documentation.dto;

public class SoapDxRequest {

    private String dxCode;
    private String dxName;
    private String dxSource;
    private Boolean main;

    public String getDxCode() {
        return dxCode;
    }

    public void setDxCode(String dxCode) {
        this.dxCode = dxCode;
    }

    public String getDxName() {
        return dxName;
    }

    public void setDxName(String dxName) {
        this.dxName = dxName;
    }

    public String getDxSource() {
        return dxSource;
    }

    public void setDxSource(String dxSource) {
        this.dxSource = dxSource;
    }

    public Boolean getMain() {
        return main;
    }

    public void setMain(Boolean main) {
        this.main = main;
    }
}
