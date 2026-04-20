# DB 스키마 및 샘플 데이터

## 스키마·테이블 위치

- **스키마**: `HOSPITAL` (Oracle)
- **애플리케이션**: `application.yml`에서 `default_schema: HOSPITAL` 로 이 스키마를 참조합니다.
- **테이블**: 아래 8개 테이블이 모두 **HOSPITAL** 스키마 안에 있습니다.

| 용도 | 테이블명 | PK 컬럼 | 비고 |
|------|----------|---------|------|
| 진료 | **CLINICAL_VISIT** | VISIT_ID | visitId 여기 있음 |
| 진료 상태 이력 | CLINICAL_VISIT_STATUS_HISTORY | HISTORY_ID | VISIT_ID FK |
| 대기열 | CLINICAL_VISIT_QUEUE | QUEUE_ID | VISIT_ID FK |
| 진료기록 | NOTE | NOTE_ID | VISIT_ID FK |
| 진단 | CLINICAL_DIAGNOSIS | DIAGNOSIS_ID | NOTE_ID FK |
| 오더 | CLINICAL_ORDER | ORDER_ID | VISIT_ID FK |
| 오더 항목 | CLINICAL_ORDER_ITEM | ORDER_ITEM_ID | ORDER_ID FK |
| 오더 결과 | CLINICAL_ORDER_RESULT | RESULT_ID | ORDER_ITEM_ID FK |

**visitId=1 인 데이터**는 **HOSPITAL.CLINICAL_VISIT** 테이블의 **VISIT_ID = 1** 인 행을 말합니다.

---

## visitId=1 존재 여부 확인 (Oracle에서 실행)

```sql
SELECT VISIT_ID, PATIENT_ID, DOCTOR_ID, RECEPTION_ID, VISIT_STATUS, START_TIME
  FROM HOSPITAL.CLINICAL_VISIT
 WHERE VISIT_ID = 1;
```

- 결과가 **1건**이면 visitId 1번 데이터 있음.
- 결과가 **0건**이면 없음 → 아래 샘플 데이터 스크립트 실행.

---

## 샘플 데이터 삽입

`docs/oracle-hospital-sample-data.sql` 파일을 **HOSPITAL 사용자로 접속한 뒤** 순서대로 실행하면 됩니다.  
(이미 VISIT_ID=1 이 있으면 해당 INSERT는 중복 오류가 날 수 있으므로, 필요 시 스크립트에서 해당 부분만 건너뛰거나 조건부 삽입을 사용하세요.)
