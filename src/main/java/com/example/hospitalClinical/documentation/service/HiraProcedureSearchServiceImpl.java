package com.example.hospitalClinical.documentation.service;

import com.example.hospitalClinical.common.client.external.hira.HiraApiClient;
import com.example.hospitalClinical.common.client.external.hira.HiraMdfeeResponseParser;
import com.example.hospitalClinical.documentation.dto.HiraProcedureSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HiraProcedureSearchServiceImpl implements HiraProcedureSearchService {

    private final HiraApiClient hiraApiClient;
    private final HiraMdfeeResponseParser hiraMdfeeResponseParser;

    @Override
    public HiraProcedureSearchResult searchProcedures(int pageNo, int numOfRows, String korNmQuery) {
        String raw = hiraApiClient.fetchDiagnosisMdfeeList(pageNo, numOfRows, korNmQuery);
        return hiraMdfeeResponseParser.parse(raw, pageNo, numOfRows);
    }
}
