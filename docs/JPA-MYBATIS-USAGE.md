# JPA / MyBatis 사용 기준

- **기본**: 단일/복합 엔티티 CRUD, 단순 조건 조회, 트랜잭션 내 변경 → **JPA (Spring Data JPA)** 사용.
- **예외**: 복잡 조회(조인·집계·서브쿼리), DTO 전용 리스트, 리포팅·통계 → **MyBatis** 사용.

## JPA로 처리하는 것

| 구분 | 설명 | 예시 |
|------|------|------|
| 엔티티 CRUD | 한 테이블/엔티티 단위 생성·조회·수정·삭제 | Visit, ClinicalNote, MedicalOrder 저장/수정/삭제 |
| 단순 조회 | findBy조건, 단일/목록 조회 | getVisit(id), listOrdersByVisitId(visitId) |
| 연관 로딩 | 엔티티 + 연관 컬렉션 한 번에 조회 | Order + OrderItems (JPQL fetch join) |

## MyBatis로 처리하는 것

| 구분 | 설명 | 예시 |
|------|------|------|
| 집계·서브쿼리 | COUNT, SUM 등과 함께 목록 조회 | 오더 목록 + 항목 개수(ORDER_ITEM COUNT) |
| 다테이블 조인 DTO | 여러 테이블 조인 결과를 DTO로만 매핑 | Visit + 마지막 상태 이력 한 건 등 |
| 복잡 조건/정렬 | 동적 WHERE, 복잡 ORDER BY | 검색 필터 다수, 정렬 조건 조합 |
| 리포팅/통계 | 읽기 전용 대시/통계 쿼리 | 일별·의사별 진료 건수 등 |

## 현재 구현 예시

- **JPA**: `MedicalOrderRepository`, `VisitRepository`, `ClinicalNoteRepository` 등 모든 엔티티 CRUD 및 `listOrdersByVisitId`(엔티티 목록).
- **MyBatis**: `OrderSummaryMapper.findOrderSummariesByVisitId` — 오더 목록 + 항목 개수(ITEM_COUNT) 한 번에 조회.  
  - API: `GET /api/visits/{visitId}/medical-orders/summary` → `OrderSummaryDTO` 목록 (orderId, visitId, orderType, orderStatus, orderDate, itemCount).

추가 기능 구현 시 위 기준으로 JPA 우선 적용하고, 조회가 복잡해지면 해당 API만 MyBatis로 분리하면 됨.
