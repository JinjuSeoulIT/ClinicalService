package com.example.hospitalClinical.order.service;

import com.example.hospitalClinical.order.dto.MedicationRecordCreateRequest;
import com.example.hospitalClinical.order.dto.MedicationRecordResponse;
import com.example.hospitalClinical.order.dto.OrderCreateRequest;
import com.example.hospitalClinical.order.dto.OrderItemCreateRequest;
import com.example.hospitalClinical.order.dto.OrderItemResponse;
import com.example.hospitalClinical.order.dto.OrderResponse;
import com.example.hospitalClinical.order.dto.TreatmentResultCreateRequest;
import com.example.hospitalClinical.order.dto.TreatmentResultResponse;
import com.example.hospitalClinical.order.entity.Order;
import com.example.hospitalClinical.order.entity.OrderItem;
import com.example.hospitalClinical.order.entity.OrderResult;

import java.util.List;


public interface OrderVisitService {

    // ==============================
    // Order (오더/처방 전체)
    // ==============================

    // 방문 기준 오더 목록 조회 (필터: 검사/처방 등)
    List<OrderResponse> listOrders(Long visitId, String orderTypeFilterOrNull);
    // 오더 생성
    OrderResponse createOrder(Long visitId, OrderCreateRequest request);

    // (Visit 기반) 오더 라인 수정 → Controller용 메서드
    OrderItemResponse updateOrderItemLine(
            Long visitId, Long orderId, Long orderItemId, OrderItemCreateRequest body);

    // (Visit 기반) 오더 라인 삭제 → Controller용 메서드
    void deleteOrderItemLine(Long visitId, Long orderId, Long orderItemId);

    // 오더 단건 조회 (Entity)
    Order getOrder(Long orderId);
    // 방문 기준 오더 목록 (Entity)
    List<Order> listOrdersByVisitId(Long visitId);
    // 오더 상태 변경 (예: REQUESTED → COMPLETED)
    Order updateOrderStatus(Long visitId, Long orderId, String orderStatus);
    // 오더 취소
    Order cancelOrder(Long visitId, Long orderId);
    // 진료지원서비스에서 상태 동기화
    Order syncOrderStatusFromSupport(Long visitId, Long orderId, String orderStatus);

    // ==============================
    //  OrderItem (오더 상세 라인)
    // ==============================

    // 오더 라인 생성
    OrderItem createOrderItem(Long orderId, OrderItemCreateRequest request);
    // 오더 라인 단건 조회
    OrderItem getOrderItem(Long orderItemId);
    // 특정 오더에 속한 라인 목록 조회
    List<OrderItem> listOrderItemsByOrderId(Long orderId);
    // 오더 라인 수정
    OrderItem updateOrderItem(Long orderItemId, OrderItemCreateRequest request);
    // 오더 라인 삭제
    void deleteOrderItem(Long orderId, Long orderItemId);

    // ==============================
    // 3️⃣ Prescription (처방 특화)
    // ==============================

    // 처방 라인 수정 (OrderItem과 동일하지만 의미상 분리)
    OrderItem updatePrescriptionItem(Long visitId, Long orderId, Long orderItemId, OrderItemCreateRequest request);
    // 처방 라인 삭제
    void deletePrescriptionItem(Long visitId, Long orderId, Long orderItemId);

    // ==============================
    // 4️⃣ OrderResult (검사 결과)
    // ==============================

    // 검사 결과 생성
    OrderResult createOrderResult(Long orderItemId, String resultValue, String resultStatus);
    // 검사 결과 조회
    OrderResult getOrderResult(Long resultId);
    // 특정 오더 라인의 결과 목록 조회
    List<OrderResult> listOrderResultsByOrderItemId(Long orderItemId);
    // 검사 결과 수정
    OrderResult updateOrderResult(Long resultId, String resultValue, String resultStatus);

    // ==============================
    // 5️⃣ 투약기록 / 처치결과 (진료 DB + 진료지원 연동)
    // ==============================

    MedicationRecordResponse createMedicationRecord(Long visitId, MedicationRecordCreateRequest request);

    TreatmentResultResponse createTreatmentResult(Long visitId, TreatmentResultCreateRequest request);

    List<MedicationRecordResponse> listMedicationRecordsByVisit(Long visitId);

    List<TreatmentResultResponse> listTreatmentResultsByVisit(Long visitId);
}