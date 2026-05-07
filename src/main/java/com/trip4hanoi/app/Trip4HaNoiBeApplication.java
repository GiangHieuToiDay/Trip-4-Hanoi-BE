package com.trip4hanoi.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Trip4HaNoiBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(Trip4HaNoiBeApplication.class, args);
    }

}
