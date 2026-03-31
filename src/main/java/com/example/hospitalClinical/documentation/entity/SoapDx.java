package com.example.hospitalClinical.documentation.entity;

import com.example.hospitalClinical.documentation.DiagnosisDxSource;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "VISIT_SOAP_DIAGNOSIS")
public class SoapDx {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visit_soap_dx_seq_gen")
    @SequenceGenerator(name = "visit_soap_dx_seq_gen", sequenceName = "CL_VSOAP_DX_SEQ", allocationSize = 1)
    @Column(name = "DIAGNOSIS_ID", nullable = false)
    private Long diagnosisId;

    @Column(name = "VISIT_ID", nullable = false)
    private Long visitId;

    @Column(name = "DX_CODE", length = 50)
    private String dxCode;

    @Column(name = "DX_NAME", length = 500)
    private String dxName;

    @Enumerated(EnumType.STRING)
    @Column(name = "DX_SOURCE", length = 24)
    private DiagnosisDxSource dxSource;

    @Column(name = "MAIN_YN", length = 1, nullable = false)
    private String mainYn;

    @Column(name = "SORT_ORDER", nullable = false)
    private Integer sortOrder;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    protected SoapDx() {}

    public static SoapDx create(
            Long visitId, String dxCode, String dxName, boolean main, int sortOrder, DiagnosisDxSource dxSource) {
        SoapDx d = new SoapDx();
        d.visitId = visitId;
        d.dxCode = dxCode;
        d.dxName = dxName;
        d.dxSource = dxSource != null ? dxSource : DiagnosisDxSource.MANUAL;
        d.mainYn = main ? "Y" : "N";
        d.sortOrder = sortOrder;
        return d;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void setMainYn(String mainYn) {
        this.mainYn = mainYn;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getDiagnosisId() {
        return diagnosisId;
    }

    public Long getVisitId() {
        return visitId;
    }

    public String getDxCode() {
        return dxCode;
    }

    public String getDxName() {
        return dxName;
    }

    public DiagnosisDxSource getDxSource() {
        return dxSource;
    }

    public String getMainYn() {
        return mainYn;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
