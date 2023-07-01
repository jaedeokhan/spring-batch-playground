package com.example.health.config;

import com.example.health.healthcheck.HealthCheck;
import com.example.health.test.Test;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(CustomProperties.class)
public class CustomConfiguration {

    @Bean
    public CustomConfig customConfig(CustomProperties properties) {
        validateCustomConfig(properties);
        CustomConfig config = new CustomConfig(properties);
        return config;
    }

    private void validateCustomConfig(CustomProperties properties) {

        if (properties == null) {
            log.info("properties null");
        }

        logHealthCheck(properties.getHealthCheck());

        logTest(properties.getTest());
    }


    private void logHealthCheck(HealthCheck healthCheck) {
        log.info("healthCheck.isEnabled() : {}", healthCheck.isEnabled());
        log.info("healthCheck.getServletUri() : {}", healthCheck.getServletUri());
    }

    private void logTest(Test test) {
        log.info("test.isEnabled() : {}", test.isEnabled());
        log.info("test.getServletUri() : {}", test.getServletUri());
    }
}
