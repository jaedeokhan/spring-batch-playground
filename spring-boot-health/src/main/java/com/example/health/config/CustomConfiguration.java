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

        validateHealthCheck(properties.getHealthCheck());

        validateTest(properties.getTest());
    }


    private void validateHealthCheck(HealthCheck healthCheck) {
        if (healthCheck == null) {
            log.info("health check null");
        }
        log.info("healthCheck.isEnabled() : {}", healthCheck.isEnabled());
        log.info("healthCheck.getServletUri() : {}", healthCheck.getServletUri());
    }

    private void validateTest(Test test) {
        if (test == null) {
            log.info("test null");
        }
        log.info("test.isEnabled() : {}", test.isEnabled());
        log.info("test.getServletUri() : {}", test.getServletUri());
    }
}
