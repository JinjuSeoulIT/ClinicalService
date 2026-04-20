# Clinical MSA (Hospital Clinical)

Spring Boot 기반 **진료(Clinical)** 도메인 REST API 서비스입니다. 외래 접수 이후 **진료 세션(Visit)** 단위로 차트·오더·문서화·활력/문진 등을 다루며, **접수(Reception)**·**수납(Billing)**·**진료지원(Clinical Support)** MSA와 HTTP·Kafka로 느슨하게 연동합니다.

---

## 주요 기능

- **접수 연동**: 접수 대기열·접수 단건 조회, 진료 시작 시 접수 상태 연동 (`RECEPTION_API_BASE_URL`)
- **진료 세션(Visit)**: 생성·조회·상태 변경·종료(종료 시 수납 청구 연동 옵션)
- **임상 차트**: 진료노트(주관적 기록), 상병(SOAP Dx), 처방(SOAP Rx) 및 마스터 상병·약품 검색 연동
- **오더(검사·처치·약물 등)**: 방문별 오더 CRUD, 오더 아이템·검사 결과, 진료지원으로 Kafka 아웃바운드
- **과거력(Past history)**: 방문별 과거력 CRUD
- **활력·문진(Clinical Vital Assess)**: 방문별 조회·저장
- **투약 기록 / 처치 결과**: 방문 단위 등록 API(진료 DB + 진료지원 연동 경로)
- **스케줄**: 미종료 방문 자동 정리 등 배치 옵션 (`app.stale-visit-auto-close`)

---

## 기술 스택

| 구분 | 내용 |
|------|------|
| Core | Java 17, Spring Boot 3.4.x, Gradle |
| Web | Spring Web (REST), Spring Validation |
| Persistence | Spring Data JPA (Hibernate), Oracle JDBC (`ojdbc11`) |
| 기타 | MyBatis 스타터(매퍼 미사용 시에도 경고 로그만 발생 가능), Lombok |
| 메시징 | Spring Cloud Stream + Kafka Binder (진료지원 연동 이벤트) |
| DB | Oracle (프로필별 `SSH` 스키마 등 설정 가능), 로컬용 `dev-h2` 프로필 |

---

## 아키텍처

- **중심 엔티티**: `Visit`(진료 세션) — `patientId`, `receptionId`, `doctorId`(담당의, STAFF 비즈니스 키 문자열 등) 식별자 기반 연동
- **오더**: `CLINICAL_ORDER` / `CLINICAL_ORDER_ITEM` — 방문(`visitId`) 소속, `DOCTOR_ID`는 요청·Visit·접수 조회 순으로 보완 가능
- **외부 HTTP**: 접수 API, 수납 API, 공공데이터(약품·질병·HIRA 등) — `application.yml`의 `reception`, `billing`, `drug`, `disease`, `hira` 설정
- **진료지원 인바운드**: `/api/clinical-support/visits/{visitId}/orders/...` — 오더 상태 동기화
- **응답 래퍼**: 대부분 `ApiResponse<T>` (`success`, `message`, `result`)

---

## 요구 사항

- JDK 17
- Oracle DB (또는 `dev-h2` 프로필로 인메모리)
- 접수·수납·Kafka를 쓰는 기능 검증 시 해당 인프라 가동
- Gradle Wrapper (`./gradlew`)

---

## 환경 변수 · 설정 (백엔드)

프로필별로 `application.yml`을 참고합니다. 대표적으로 다음을 환경 변수로 두는 것을 권장합니다.

| 변수 | 설명 |
|------|------|
| `SPRING_PROFILES_ACTIVE` | `local`, `dev`, `dev-h2`, `prod` 등 |
| `SERVER_PORT` | 기본 `8090` (`prod` 기본 예시는 `8080`) |
| `DEV_DB_URL`, `DEV_DB_USERNAME`, `DEV_DB_PASSWORD` | `dev` 프로필 Oracle 접속 |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `prod` |
| `RECEPTION_API_BASE_URL` | 접수 MSA 베이스 URL |
| `BILLING_API_BASE_URL`, `BILLING_API_ENABLED` | 수납 연동 |
| `KAFKA_BROKERS`, `KAFKA_BROKER_PORT` | Kafka 브로커 |
| `CLINICAL_SUPPORT_ORDER_ENABLED` | 진료지원 오더 이벤트 발행 여부 |
| `HIRA_API_KEY`, `DRUG_API_KEY`, `DISEASE_API_KEY` | 공공 API 키(해당 기능 사용 시) |

---

## 실행 방법 (백엔드)

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

IDE에서는 `HospitalClinicalApplication` 실행 시 VM 옵션 또는 Active profiles에 `dev` 등을 지정합니다.  
로컬에서 Oracle 대신 H2만 쓰려면 `--spring.profiles.active=dev-h2` 를 사용합니다.

---

## 주요 API 경로 (요약)

| 영역 | Base path | 설명 |
|------|------------|------|
| 진료 대시보드 | `GET /api/clinical`, `GET /api/clinical/reception-queue`, `POST /api/clinical/start` 등 | 진료 목록·대기열·진료 시작 |
| 방문 | `/api/visits/{visitId}` | 조회·상태·종료 |
| 활력·문진 | `/api/visits/{visitId}/vital-assess` | GET / PUT |
| 약품·수가 검색 | `/api/visits/{visitId}/drug-search`, `/procedure-search` | 진료 맥락 검색 |
| 진료노트·상병·처방 | `/api/visits/{visitId}/notes`, `/api/notes/{noteId}/diagnosis`, SOAP 컨트롤러 경로 | 문서화 |
| 오더 | `/api/visits/{visitId}/orders` | 목록은 `orderType` 쿼리 필터 가능, 정렬은 저장소 JPQL 기준(예: `orderDate DESC`) |
| 오더(레거시) | `GET/POST /api/orders` | Deprecated — 가능하면 방문 경로 사용 |
| 과거력 | `/api/visits/{visitId}/past-history` | |
| 진료지원 | `/api/clinical-support/visits/{visitId}/orders/...` | 인바운드 동기화 |
| Actuator | `/actuator` | 노출 범위는 운영 정책에 맞게 조정 |

---

## 프론트엔드 연동 (HIS Frontend — 진료 영역)

진료 화면은 **별도 저장소(예: `frontend`)** 의 Next.js 앱에서 동작하며, 진료 API 베이스 URL만 본 MSA를 가리키면 됩니다.

### 환경 변수

| 변수 | 설명 |
|------|------|
| `NEXT_PUBLIC_CLINICAL_API_BASE_URL` | 본 서비스 베이스 URL (코드 기본 예: `http://192.168.1.70:8090`, 미설정 시 해당 값 사용) |

`.env.local`에 실제 호스트·포트를 맞춥니다. 다른 MSA와 마찬가지로 **값이 비어 있으면** 프론트 모듈이 기본 호스트에 붙거나 동작이 어긋날 수 있으니 반드시 확인합니다.

### 실행 예시 (프론트)

```bash
npm install
npm run dev
```

접속 URL은 프로젝트 설정에 따릅니다(예: `http://localhost:3001`).

### 코드 위치 (참고)

| 구분 | 경로(예시) |
|------|------------|
| 진료 화면 | `src/components/clinical/` (`Clinical.tsx`, `ClinicalOrder.tsx`, `dialogs/` 등) |
| 상태·Saga | `src/features/clinical/` |
| API 클라이언트 | `src/lib/clinical/` (`clinicalApiBase.ts`, `visitApi.ts`, `clinicalOrderApi.ts`, `clinicalVitalsApi.ts`, `visitMedicationTreatmentApi.ts` 등) |

`CLINICAL_API_BASE`는 `NEXT_PUBLIC_CLINICAL_API_BASE_URL`에서 읽습니다.

---

## 주의사항

- **DB 스키마·권한**: `dev`/`prod`에서 `default_schema: SSH` 등 실제 계정에 맞게 조정해야 합니다.
- **접수 `doctorId`**: 문자열(STAFF ID) 형태가 될 수 있어, 타입을 숫자만 가정하면 역직렬화 오류가 납니다.
- **Kafka**: 브로커가 없으면 바인더 기동·발행 관련 기능이 실패할 수 있습니다. 로컬만 검증할 때는 프로필·`CLINICAL_SUPPORT_ORDER_ENABLED` 등으로 범위를 줄입니다.
- **CORS**: 컨트롤러에 허용 Origin이 박혀 있으므로, 새 프론트 포트를 쓰면 서버 CORS 목록도 함께 수정해야 합니다.

---

## 트러블슈팅

| 증상 | 확인 |
|------|------|
| 진료 목록·대기열이 비거나 5xx | `RECEPTION_API_BASE_URL`, 접수 서버 기동, 네트워크 |
| 프론트에서 진료 API만 실패 | `NEXT_PUBLIC_CLINICAL_API_BASE_URL`, 본 서버 포트(`SERVER_PORT`) |
| DB 연결 실패 | 프로필별 `datasource` URL·계정·방화벽 |
| 오더 `DOCTOR_ID`가 NULL | 오더 생성 요청의 `doctorId` 전달 여부, Visit·접수의 담당의 보존 여부 |
| Kafka 관련 오류 | `KAFKA_BROKERS`, 브로커 가동 여부, 토픽 정책 |

---

## 저장소·모듈

- **백엔드(본 README 위치)**: `hospital-clinical` — 패키지 루트 `com.example.hospitalClinical`
- **프론트**: 동일 HIS 내 `frontend` — 진료 UI는 `clinical` 접두 경로 모듈과 연동

문의·스프린트 범위는 팀 스크럼 기준으로 정리하면 됩니다.
