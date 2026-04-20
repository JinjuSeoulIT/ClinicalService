# DB 테이블·시퀀스 정리 (엔티티 기준)

모든 테이블은 **스키마 HOSPITAL** 아래, **CLINICAL_** 접두사**로 생성합니다.

| Entity | 테이블 |
|--------|--------|
| Visit | CLINICAL_VISIT |
| VisitStatusHistory | CLINICAL_VISIT_STATUS_HISTORY |
| VisitQueue | CLINICAL_VISIT_QUEUE |
| Note | NOTE |
| Diagnosis | CLINICAL_DIAGNOSIS |
| Order | CLINICAL_ORDER |
| OrderItem | CLINICAL_ORDER_ITEM |
| OrderResult | CLINICAL_ORDER_RESULT |

---

## 1. encounter (진료 흐름)

### 1) CLINICAL_VISIT
| 컬럼명 | 타입 | nullable | 설명 |
|--------|------|----------|------|
| VISIT_ID | NUMBER(19) / BIGINT | PK | 진료 세션 ID (시퀀스: **CL_VISIT_SEQ**) |
| PATIENT_ID | NUMBER(19) / BIGINT | Y | 환자 ID |
| DOCTOR_ID | NUMBER(19) / BIGINT | Y | 의사 ID |
| RECEPTION_ID | NUMBER(19) / BIGINT | Y | 접수 ID |
| VISIT_STATUS | VARCHAR(20) | Y | 상태 (WAITING, IN_PROGRESS, COMPLETED 등) |
| START_TIME | TIMESTAMP | Y | 진료 시작 시각 |
| END_TIME | TIMESTAMP | Y | 진료 종료 시각 |
| CREATED_AT | TIMESTAMP | Y | 생성 시각 |
| UPDATED_AT | TIMESTAMP | Y | 수정 시각 |

### 2) CLINICAL_VISIT_STATUS_HISTORY
| 컬럼명 | 타입 | nullable | 설명 |
|--------|------|----------|------|
| HISTORY_ID | NUMBER(19) / BIGINT | PK | 이력 ID (시퀀스: **CL_VISIT_STATUS_HIST_SEQ**) |
| VISIT_ID | NUMBER(19) / BIGINT | N | CLINICAL_VISIT FK |
| STATUS | VARCHAR(20) | Y | waiting / in_progress / completed 등 |
| CHANGED_AT | TIMESTAMP | Y | 변경 시각 |

### 3) CLINICAL_VISIT_QUEUE
| 컬럼명 | 타입 | nullable | 설명 |
|--------|------|----------|------|
| QUEUE_ID | NUMBER(19) / BIGINT | PK | 대기열 ID (시퀀스: **CL_VISIT_QUEUE_SEQ**) |
| VISIT_ID | NUMBER(19) / BIGINT | N | CLINICAL_VISIT FK |
| QUEUE_ORDER | INTEGER | Y | 대기 순서 |
| ROOM_ID | NUMBER(19) / BIGINT | Y | 진료실 ID |
| CREATED_AT | TIMESTAMP | Y | 생성 시각 |

---

## 2. documentation (진료 기록)

### 4) NOTE
| 컬럼명 | 타입 | nullable | 설명 |
|--------|------|----------|------|
| NOTE_ID | NUMBER(19) / BIGINT | PK | 진료기록 ID (시퀀스: **CL_NOTE_SEQ**) |
| VISIT_ID | NUMBER(19) / BIGINT | N | CLINICAL_VISIT FK |
| CHIEF_COMPLAINT | VARCHAR(2000) | Y | 주소호 |
| PRESENT_ILLNESS | VARCHAR(4000) | Y | 현병력 |
| ASSESSMENT | VARCHAR(4000) | Y | 평가 |
| PLAN | VARCHAR(4000) | Y | 치료 계획 |
| MEMO | VARCHAR(2000) | Y | 메모 |
| STATUS | VARCHAR(20) | Y | DRAFT 등 |
| CREATED_AT | TIMESTAMP | Y | 생성 시각 |
| UPDATED_AT | TIMESTAMP | Y | 수정 시각 |

### 5) CLINICAL_DIAGNOSIS
| 컬럼명 | 타입 | nullable | 설명 |
|--------|------|----------|------|
| DIAGNOSIS_ID | NUMBER(19) / BIGINT | PK | 진단 ID (시퀀스: **CL_DIAGNOSIS_SEQ**) |
| NOTE_ID | NUMBER(19) / BIGINT | N | NOTE FK |
| PATIENT_CODE | VARCHAR(50) | Y | 환자 코드 |
| DIAGNOSIS_CODE | VARCHAR(50) | Y | 진단 코드 |
| DESCRIPTION | VARCHAR(1000) | Y | 설명 |
| CREATED_AT | TIMESTAMP | Y | 생성 시각 |

---

## 3. order (검사/처치/처방)

### 6) CLINICAL_ORDER
| 컬럼명 | 타입 | nullable | 설명 |
|--------|------|----------|------|
| ORDER_ID | NUMBER(19) / BIGINT | PK | 오더 ID (시퀀스: **CL_ORDER_SEQ**) |
| VISIT_ID | NUMBER(19) / BIGINT | N | CLINICAL_VISIT FK |
| ORDER_TYPE | VARCHAR(20) | Y | LAB / PROCEDURE / PRESCRIPTION 등 |
| ORDER_STATUS | VARCHAR(20) | Y | REQUESTED / CANCELLED 등 |
| DOCTOR_ID | NUMBER(19) / BIGINT | Y | 의사 ID |
| ORDER_DATE | TIMESTAMP | Y | 오더 일시 |
| CREATED_AT | TIMESTAMP | Y | 생성 시각 |
| UPDATED_AT | TIMESTAMP | Y | 수정 시각 |

### 7) CLINICAL_ORDER_ITEM
| 컬럼명 | 타입 | nullable | 설명 |
|--------|------|----------|------|
| ORDER_ITEM_ID | NUMBER(19) / BIGINT | PK | 항목 ID (시퀀스: **CL_ORDER_ITEM_SEQ**) |
| ORDER_ID | NUMBER(19) / BIGINT | N | CLINICAL_ORDER FK |
| ITEM_CODE | VARCHAR(50) | Y | 항목 코드 |
| DOSE | NUMBER(10,2) / DECIMAL(10,2) | Y | 투여량 |
| FREQUENCY | VARCHAR(100) | Y | 투여 빈도 |
| DURATION | VARCHAR(100) | Y | 투여 기간 |
| CREATED_AT | TIMESTAMP | Y | 생성 시각 |

### 8) CLINICAL_ORDER_RESULT
| 컬럼명 | 타입 | nullable | 설명 |
|--------|------|----------|------|
| RESULT_ID | NUMBER(19) / BIGINT | PK | 결과 ID (시퀀스: **CL_ORDER_RESULT_SEQ**) |
| ORDER_ITEM_ID | NUMBER(19) / BIGINT | N | CLINICAL_ORDER_ITEM FK |
| RESULT_VALUE | VARCHAR(2000) | Y | 결과 값 |
| RESULT_STATUS | VARCHAR(20) | Y | PENDING / COMPLETED 등 |
| RESULT_DATE | TIMESTAMP | Y | 결과 일시 |
| CREATED_AT | TIMESTAMP | Y | 생성 시각 |

---

## 요약: 만들어야 할 것

| 구분 | 이름 |
|------|------|
| **스키마** | HOSPITAL (Oracle: CREATE SCHEMA 또는 USER, H2: CREATE SCHEMA HOSPITAL) |
| **시퀀스(8개, 30자 이내)** | CL_VISIT_SEQ, CL_VISIT_STATUS_HIST_SEQ, CL_VISIT_QUEUE_SEQ, CL_NOTE_SEQ, CL_DIAGNOSIS_SEQ, CL_ORDER_SEQ, CL_ORDER_ITEM_SEQ, CL_ORDER_RESULT_SEQ |
| **테이블(8개)** | CLINICAL_VISIT, CLINICAL_VISIT_STATUS_HISTORY, CLINICAL_VISIT_QUEUE, NOTE, CLINICAL_DIAGNOSIS, CLINICAL_ORDER, CLINICAL_ORDER_ITEM, CLINICAL_ORDER_RESULT |

---

## 참고

- **Oracle**: `NUMBER(19)` = Long, `VARCHAR(n)`, `TIMESTAMP`(또는 `DATE`). 시퀀스는 `CREATE SEQUENCE HOSPITAL.VISIT_SEQ ...` 형태로 생성.
- **H2**: `BIGINT`, `VARCHAR(n)`, `TIMESTAMP`. 시퀀스 지원.
- **MySQL**: `BIGINT`, `VARCHAR(n)`, `DATETIME`/`TIMESTAMP`. AUTO_INCREMENT로 대체 가능하지만, 엔티티가 시퀀스 이름을 쓰고 있으므로 DB에서 시퀀스 지원 여부 확인 필요.
- **로컬(local 프로필)**: `application.yml`에서 H2 + `ddl-auto: create-drop` 이면 스키마·테이블·시퀀스를 애플리케이션이 생성하므로, 별도 DDL 없이 실행만 해도 됨.
