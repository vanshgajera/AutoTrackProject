package com.autotrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutotrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutotrackApplication.class, args);

    }

}