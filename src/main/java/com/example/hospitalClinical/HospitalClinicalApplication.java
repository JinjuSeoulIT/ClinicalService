package com.example.hospitalClinical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HospitalClinicalApplication {

    public static void main(String[] args) {        //Java 프로그램 실행 시 제일 먼저 실행되는 메서드.
        SpringApplication.run(HospitalClinicalApplication.class, args);  /*서버를 띄우고, 이후엔 HTTP 요청이
                                                                           들어올 때마다 Controller 등이 실행됨.*/

    }
}
