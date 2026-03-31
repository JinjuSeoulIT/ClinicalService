package com.hospital.billing.facade;

import com.hospital.billing.dto.integration.ClinicalCompletedRequest;
import com.hospital.billing.dto.integration.ClinicalCompletedResult;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.BillItem;
import com.hospital.billing.entity.BillItemSource;
import com.hospital.billing.entity.BillingRequest;
import com.hospital.billing.repository.BillItemRepository;
import com.hospital.billing.repository.BillItemSourceRepository;
import com.hospital.billing.repository.BillRepository;
import com.hospital.billing.service.BillingRequestService;
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
    private final BillItemRepository billItemRepository;
    private final BillItemSourceRepository billItemSourceRepository;

    // [수정]
    // BillingRequestRepository 직접 사용 대신 별도 서비스 사용
    private final BillingRequestService billingRequestService;

    public BillingFacade(BillRepository billRepository,
                         BillItemRepository billItemRepository,
                         BillItemSourceRepository billItemSourceRepository,
                         BillingRequestService billingRequestService) {
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.billItemSourceRepository = billItemSourceRepository;
        this.billingRequestService = billingRequestService;
    }

    @Transactional
    public ClinicalCompletedResult handleClinicalCompleted(ClinicalCompletedRequest request) {

        validateRequest(request);

        // 기존 요청 이력 기준 중복 확인
        BillingRequest existingRequest = billingRequestService
                .findByEventId(request.getEventId())
                .orElse(null);

        if (existingRequest != null) {
            if (existingRequest.getBillId() != null) {
                return new ClinicalCompletedResult(existingRequest.getBillId(), true);
            }

            Bill existingBillByEventId = billRepository
                    .findBySourceEventId(request.getEventId())
                    .orElse(null);

            if (existingBillByEventId != null) {

                // 요청 이력은 별도 트랜잭션으로 성공 처리
                billingRequestService.markSuccess(
                        existingRequest.getId(),
                        existingBillByEventId.getId()
                );
                return new ClinicalCompletedResult(existingBillByEventId.getId(), true);
            }

            throw new IllegalStateException("이미 접수된 요청입니다. eventId=" + request.getEventId());
        }


        // 요청 수신 이력 저장을 별도 트랜잭션으로 분리
        BillingRequest billingRequest = billingRequestService.saveReceived(request);

        try {
            // 1. eventId 기준 기존 bill 존재 여부 확인
            Bill existingByEventId = billRepository
                    .findBySourceEventId(request.getEventId())
                    .orElse(null);

            if (existingByEventId != null) {

                billingRequestService.markSuccess(
                        billingRequest.getId(),
                        existingByEventId.getId()
                );
                return new ClinicalCompletedResult(existingByEventId.getId(), true);
            }

            // 2. visitId 기준 기존 bill 존재 여부 확인
            Bill existingByVisitId = billRepository
                    .findByVisitId(request.getVisitId())
                    .orElse(null);

            if (existingByVisitId != null) {
                // [추가]
                billingRequestService.markSuccess(
                        billingRequest.getId(),
                        existingByVisitId.getId()
                );
                return new ClinicalCompletedResult(existingByVisitId.getId(), true);
            }


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

            // 6. BillItem 생성 후 저장
            List<BillItem> billItems = new ArrayList<>();
            for (TempBillItem tempItem : tempItems) {
                BillItem billItem = BillItem.create(
                        savedBill,
                        tempItem.getItemName(),
                        tempItem.getAmount()
                );
                billItems.add(billItem);
            }

            List<BillItem> savedBillItems = billItemRepository.saveAll(billItems);

            // 7. BILL_ITEM_SOURCE 저장
            List<BillItemSource> billItemSources = new ArrayList<>();
            for (int i = 0; i < savedBillItems.size(); i++) {
                BillItem savedBillItem = savedBillItems.get(i);
                TempBillItem tempItem = tempItems.get(i);

                BillItemSource billItemSource = BillItemSource.create(
                        savedBillItem,
                        request.getVisitId(),
                        tempItem.getSourceType(),
                        tempItem.getSourceId(),
                        request.getEventId(),
                        Timestamp.valueOf(LocalDateTime.now())
                );
                billItemSources.add(billItemSource);
            }
            billItemSourceRepository.saveAll(billItemSources);


            // 성공 상태 업데이트를 별도 트랜잭션으로 처리
            billingRequestService.markSuccess(
                    billingRequest.getId(),
                    savedBill.getId()
            );

            // 8. 결과 반환
            return new ClinicalCompletedResult(savedBill.getId(), false);

        } catch (Exception e) {

            // 실패 상태 업데이트를 별도 트랜잭션으로 처리
            billingRequestService.markFailed(
                    billingRequest.getId(),
                    e.getMessage()
            );
            throw e;
        }
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

    // 임시 항목 생성 메서드
    // 실제 연동 전 단계이므로 하드코딩된 항목 사용
    // 추후 진료 근거 시스템 연동 시 이 부분이 실제 조회 로직으로 교체될 예정
    private List<TempBillItem> createTemporaryItems(Long visitId) {
        List<TempBillItem> items = new ArrayList<>();

        // 현재는 SOURCE_TYPE / SOURCE_ID도 임시 하드코딩으로 함께 넣음
        // 추후 실제 진료 근거 연동 시 원본 테이블 PK / 타입으로 교체 예정
        items.add(new TempBillItem("초진 진찰료", 10000, "CLINICAL", 1L));
        items.add(new TempBillItem("혈액검사", 20000, "TEST", 2L));
        items.add(new TempBillItem("주사 처치", 5000, "TREATMENT", 3L));

        return items;
    }

    private static class TempBillItem {
        private final String itemName;
        private final Integer amount;
        private final String sourceType;
        private final Long sourceId;

        public TempBillItem(String itemName,
                            Integer amount,
                            String sourceType,
                            Long sourceId) {
            this.itemName = itemName;
            this.amount = amount;
            this.sourceType = sourceType;
            this.sourceId = sourceId;
        }

        public String getItemName() {
            return itemName;
        }

        public Integer getAmount() {
            return amount;
        }

        public String getSourceType() {
            return sourceType;
        }

        public Long getSourceId() {
            return sourceId;
        }
    }
}