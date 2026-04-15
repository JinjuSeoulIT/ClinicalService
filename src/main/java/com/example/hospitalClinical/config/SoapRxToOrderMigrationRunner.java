package com.example.hospitalClinical.config;

import com.example.hospitalClinical.documentation.entity.SoapRx;
import com.example.hospitalClinical.documentation.repository.SoapRxRepo;
import com.example.hospitalClinical.encounter.entity.Visit;
import com.example.hospitalClinical.encounter.repository.VisitRepo;
import com.example.hospitalClinical.order.entity.OrderType;
import com.example.hospitalClinical.order.entity.Order;
import com.example.hospitalClinical.order.entity.OrderItem;
import com.example.hospitalClinical.order.repository.OrderRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.migration.soap-rx-to-order.enabled", havingValue = "true")
public class SoapRxToOrderMigrationRunner implements ApplicationRunner {

    private final SoapRxRepo soapRxRepo;
    private final OrderRepo orderRepo;
    private final VisitRepo visitRepo;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int migrated = 0;
        for (SoapRx rx : soapRxRepo.findAll()) {
            if (orderRepo.existsByLegacyPrescriptionId(rx.getPrescriptionId())) {
                continue;
            }
            Visit visit = visitRepo.findById(rx.getVisitId()).orElse(null);
            if (visit == null) {
                log.warn("SoapRx skip: visit not found prescriptionId={} visitId={}", rx.getPrescriptionId(), rx.getVisitId());
                continue;
            }
            if (visit.getPatientId() == null) {
                log.warn("SoapRx skip: visit has no patientId prescriptionId={}", rx.getPrescriptionId());
                continue;
            }
            String name = rx.getMedicationName() != null ? rx.getMedicationName().trim() : "";
            if (name.isEmpty()) {
                log.warn("SoapRx skip: empty medicationName prescriptionId={}", rx.getPrescriptionId());
                continue;
            }
            Order o = Order.create(rx.getVisitId(), OrderType.PRESCRIPTION, "REQUESTED", null);
            o.setLegacyPrescriptionId(rx.getPrescriptionId());
            o.addItem(OrderItem.createPrescriptionLine(name, visit.getPatientId(), null, null));
            orderRepo.save(o);
            migrated++;
        }
        log.info("SoapRx → Order migration finished migratedRows={}", migrated);
    }
}
