package com.hospital.billing.facade;

import com.hospital.billing.dto.integration.ClinicalCompletedRequest;
import com.hospital.billing.dto.integration.ClinicalCompletedResult;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillItem;
import com.hospital.billing.repository.BillItemRepository;
import com.hospital.billing.repository.BillRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class BillingFacade {

    private final BillRepository billRepository;

    // BillItem 저장용 Repository 주입
    private final BillItemRepository billItemRepository;

    public BillingFacade(BillRepository billRepository,
                         BillItemRepository billItemRepository) {
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
    }

    // [추가] Bill + BillItem 저장을 하나의 트랜잭션으로 처리
    @Transactional
    public ClinicalCompletedResult handleClinicalCompleted(ClinicalCompletedRequest request) {

        validateRequest(request);

        // 1. eventId 기준 중복 체크
        Bill existingByEventId = billRepository
                .findBySourceEventId(request.getEventId())
                .orElse(null);

        if (existingByEventId != null) {
            return new ClinicalCompletedResult(existingByEventId.getId(), true);
        }

        // 2. visitId 기준 기존 bill 존재 여부 확인
        Bill existingByVisitId = billRepository
                .findByVisitId(request.getVisitId())
                .orElse(null);

        if (existingByVisitId != null) {
            return new ClinicalCompletedResult(existingByVisitId.getId(), true);
        }

        // [유지 + 주석보강]
        // 임시 계산용 항목 생성
        // 현재는 Billing 생성 흐름 검증용 하드코딩 데이터이며,
        // 추후 visit / treatment / order 연동 시 실제 진료 근거 데이터 조회로 교체 예정
        List<TempBillItem> tempItems = createTemporaryItems(request.getVisitId());

        // 총 금액 계산
        int totalAmount = tempItems.stream()
                .mapToInt(TempBillItem::getAmount)
                .sum();

        Timestamp treatmentDate = toTimestamp(request.getOccurredAt());
        Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());

        // 3. Bill 생성
        Bill bill = new Bill(
                request.getPatientId(),
                treatmentDate,
                totalAmount,
                createdAt
        );

        // 4. 연동 식별값 세팅
        bill.setVisitId(request.getVisitId());
        bill.setSourceEventId(request.getEventId());

        // 5. Bill 저장
        Bill savedBill = billRepository.save(bill);

        // [수정]
        // 현재 실제 BILL_ITEM 테이블 구조(BILL_ITEM_ID, BILL_ID, ITEM_NAME, ITEM_AMOUNT)에 맞게
        // 항목명과 금액만 저장
        List<BillItem> billItems = new ArrayList<>();
        for (TempBillItem tempItem : tempItems) {
            BillItem billItem = BillItem.create(
                    savedBill,
                    tempItem.getItemName(),
                    tempItem.getAmount()
            );
            billItems.add(billItem);
        }

        // [수정] save 반복 대신 saveAll 일괄 저장
        billItemRepository.saveAll(billItems);

        // 6. 결과 반환
        return new ClinicalCompletedResult(savedBill.getId(), false);
    }

    private void validateRequest(ClinicalCompletedRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 값이 없습니다.");
        }

        if (isBlank(request.getEventId())) {
            throw new IllegalArgumentException("eventId는 필수입니다.");
        }

        if (request.getVisitId() == null) {
            throw new IllegalArgumentException("visitId는 필수입니다.");
        }

        if (request.getPatientId() == null) {
            throw new IllegalArgumentException("patientId는 필수입니다.");
        }

        if (isBlank(request.getStatus())) {
            throw new IllegalArgumentException("status는 필수입니다.");
        }
    }

    private Timestamp toTimestamp(LocalDateTime occurredAt) {
        LocalDateTime baseTime = Objects.requireNonNullElseGet(
                occurredAt,
                LocalDateTime::now
        );
        return Timestamp.valueOf(baseTime);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // [유지 + 주석보강]
    // 임시 항목 생성 메서드
    // 실제 연동 전 단계이므로 하드코딩된 항목 사용
    // 추후 진료 근거 시스템 연동 시 이 부분이 실제 조회 로직으로 교체될 예정
    private List<TempBillItem> createTemporaryItems(Long visitId) {
        List<TempBillItem> items = new ArrayList<>();

        items.add(new TempBillItem("초진 진찰료", 10000));
        items.add(new TempBillItem("혈액검사", 20000));
        items.add(new TempBillItem("주사 처치", 5000));

        return items;
    }

    // [수정]
    // 현재 BILL_ITEM 테이블 구조에 맞게 itemName + amount만 유지
    private static class TempBillItem {
        private final String itemName;
        private final Integer amount;

        public TempBillItem(String itemName, Integer amount) {
            this.itemName = itemName;
            this.amount = amount;
        }

        public String getItemName() {
            return itemName;
        }

        public Integer getAmount() {
            return amount;
        }
    }
}