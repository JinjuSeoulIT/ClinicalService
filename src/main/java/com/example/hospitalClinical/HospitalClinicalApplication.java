package com.example.hospitalClinical;

import com.example.hospitalClinical.common.client.external.hira.HiraApiProperties;
import com.example.hospitalClinical.encounter.integration.testresult.TestResultReadyKafkaProperties;
import com.example.hospitalClinical.order.integration.clinicalsupport.inbound.SupportFeedbackKafkaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
        HiraApiProperties.class,
        TestResultReadyKafkaProperties.class,
        SupportFeedbackKafkaProperties.class
})
public class HospitalClinicalApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalClinicalApplication.class, args);
    }
}