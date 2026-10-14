package com.grovesoft.boctracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BocOnboardingTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BocOnboardingTrackerApplication.class, args);
    }
}
