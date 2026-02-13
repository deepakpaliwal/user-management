package com.uums.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UumsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UumsApiApplication.class, args);
    }
}
