package com.example.hospitalClinical.documentation.service;

import com.example.hospitalClinical.documentation.dto.HiraProcedureSearchResult;

public interface HiraProcedureSearchService {

    HiraProcedureSearchResult searchProcedures(int pageNo, int numOfRows, String korNmQuery);
}
