package com.example.hospitalClinical.common.integration.billing.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "billing.kafka.clinical-completed")
public class BillingClinicalCompletedKafkaProperties {
    /**
     * true면 진료완료(청구 생성 요청)를 Kafka로 발행합니다.
     * REST 기반 수납 연동을 대체하는 용도입니다.
     */
    private boolean enabled = true;
}

