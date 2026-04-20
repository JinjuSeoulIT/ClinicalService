package com.example.hospitalClinical.common.integration.billing.kafka;

import com.example.hospitalClinical.common.client.external.billing.BillingClinicalCompletedRequest;
import com.example.hospitalClinical.common.event.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static com.example.hospitalClinical.common.event.Event.Type.CREATE;

@Component
@RequiredArgsConstructor
public class BillingClinicalCompletedEventPublisher {

    public static final String BINDING_OUT_CLINICAL_COMPLETED = "output-clinicalCompleted-out-0";

    private final StreamBridge streamBridge;
    private final BillingClinicalCompletedKafkaProperties properties;

    public void publish(BillingClinicalCompletedRequest body) {
        if (!properties.isEnabled() || body == null) {
            return;
        }
        Object key = body.getVisitId() != null ? body.getVisitId() : body.getEventId();
        streamBridge.send(
                BINDING_OUT_CLINICAL_COMPLETED,
                MessageBuilder.withPayload(new Event<>(CREATE, key, body)).build()
        );
    }
}

