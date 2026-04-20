# Hospital Clinical 백엔드 트리 구조 (클래스 전체)

```
hospitalClinical (com.example.hospitalClinical)
│
├── HospitalClinicalApplication.java
│
├── common
│   ├── exception
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   └── ErrorCode.java
│   └── response
│       └── ApiResponse.java
│
├── encounterSessionManagement     진료세션관리
│   ├── controller
│   │   ├── VisitController.java
│   │   ├── EncounterStatusController.java
│   │   └── EncounterSummaryController.java
│   ├── service
│   │   ├── EncounterSessionManagementService.java
│   │   └── EncounterSessionManagementServiceImpl.java
│   ├── dto
│   │   ├── VisitCreateRequest.java
│   │   ├── VisitResponse.java
│   │   ├── EncounterStatusResponse.java
│   │   └── EncounterSummaryResponse.java
│   ├── entity
│   │   ├── Visit.java
│   │   ├── EncounterStatus.java
│   │   └── EncounterSummary.java
│   ├── repository
│   │   ├── VisitRepository.java
│   │   ├── EncounterStatusRepository.java
│   │   └── EncounterSummaryRepository.java
│   └── exception
│       └── VisitNotFoundException.java
│
├── clinicalDocumentationManagement   진료기록관리
│   ├── controller
│   │   ├── ClinicalNoteController.java
│   │   └── SoapSectionController.java
│   ├── service
│   │   ├── ClinicalDocumentationManagementService.java
│   │   └── ClinicalDocumentationManagementServiceImpl.java
│   ├── dto
│   │   ├── ClinicalNoteCreateRequest.java
│   │   ├── ClinicalNoteUpdateRequest.java
│   │   ├── ClinicalNoteResponse.java
│   │   └── SoapSectionResponse.java
│   ├── entity
│   │   ├── ClinicalNote.java
│   │   └── SoapSection.java
│   ├── repository
│   │   ├── ClinicalNoteRepository.java
│   │   └── SoapSectionRepository.java
│   └── exception
│       └── ClinicalNoteNotFoundException.java
│
├── medicalOrderManagement    오더관리
│   ├── controller
│   │   ├── MedicalOrderController.java
│   │   ├── OrderItemController.java
│   │   └── OrderResultController.java
│   ├── service
│   │   ├── MedicalOrderManagementService.java
│   │   └── MedicalOrderManagementServiceImpl.java
│   ├── dto
│   │   ├── MedicalOrderCreateRequest.java
│   │   ├── MedicalOrderResponse.java
│   │   ├── OrderItemCreateRequest.java
│   │   ├── OrderItemResponse.java
│   │   └── OrderResultResponse.java
│   ├── entity
│   │   ├── MedicalOrder.java
│   │   ├── OrderItem.java
│   │   └── OrderResult.java
│   ├── repository
│   │   ├── MedicalOrderRepository.java
│   │   ├── OrderItemRepository.java
│   │   └── OrderResultRepository.java
│   └── exception
│       └── MedicalOrderNotFoundException.java
│
└── mapstruct   (미사용)
```

## 요약

| 구분 | encounterSessionManagement | clinicalDocumentationManagement | medicalOrderManagement |
|------|-----------------------------|----------------------------------|-------------------------|
| controller | 3 | 3 | 3 |
| service | 2 (Interface + Impl) | 2 (Interface + Impl) | 2 (Interface + Impl) |
| dto | 4 | 5 | 5 |
| entity | 3 | 3 | 3 |
| repository | 3 | 3 | 3 |
| exception | 1 | 1 | 1 |
