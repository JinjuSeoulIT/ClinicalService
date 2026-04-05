package com.example.hospitalClinical;

import com.example.hospitalClinical.common.client.external.hira.HiraApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        HiraApiProperties.class
})
public class HospitalClinicalApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalClinicalApplication.class, args);
    }
}