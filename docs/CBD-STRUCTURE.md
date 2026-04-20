# HIS Clinical Subsystem – CBD 구조

## clinical-service (진료 서비스) 패키지 구조

```
com.example.hospitalClinical
├── common
│   ├── exception (GlobalExceptionHandler, BusinessException, ErrorCode)
│   └── response (ApiResponse)
├── encounterSessionManagement   (진료세션관리)
│   ├── controller (VisitController, EncounterStatusController, EncounterSummaryController)
│   ├── service (EncounterSessionManagementService, EncounterSessionManagementServiceImpl)
│   ├── repository, entity, dto, exception
├── clinicalDocumentationManagement (진료기록관리)
│   ├── controller (ClinicalNoteController, SoapSectionController)
│   ├── service (ClinicalDocumentationManagementService, Impl)
│   ├── repository, entity, dto, exception
└── medicalOrderManagement       (의료오더관리)
    ├── controller (MedicalOrderController, OrderItemController, OrderResultController)
    ├── service (MedicalOrderManagementService, MedicalOrderManagementServiceImpl)
    ├── repository (MedicalOrderRepository, OrderItemRepository, OrderResultRepository)
    ├── entity (MedicalOrder, OrderItem, OrderResult)
    ├── dto
    └── exception (MedicalOrderNotFoundException)
```

## API 경로

| 컴포넌트 | 경로 |
|----------|------|
| 진료세션 | `GET/POST /api/visits`, `GET/PATCH /api/visits/{visitId}`, `POST /api/visits/{visitId}/end` |
| 진료기록 | `GET/POST/PUT /api/visits/{visitId}/clinical-notes` |
| 의료오더 | `GET/POST /api/visits/{visitId}/medical-orders`, `GET/PATCH /api/visits/{visitId}/medical-orders/{orderId}`, `POST .../cancel` |

## 테이블 (HOSPITAL 스키마)

- **VISIT** – 진료(세션)
- **ENCOUNTER_STATUS** – 진료상태
- **ENCOUNTER_SUMMARY** – 진료요약
- **NOTE** – 진료기록
- **SOAP_SECTION** – SOAP 구성 (CC, PI, A, P)
- **MEDICAL_ORDER** – 의료오더 (LAB/PROCEDURE/PRESCRIPTION)
- **ORDER_ITEM** – 오더 항목
- **ORDER_RESULT** – 검사 결과

local 프로필에서는 `ddl-auto: create-drop`으로 위 테이블이 자동 생성됩니다.
