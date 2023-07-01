package com.example.health.config;

import com.example.health.healthcheck.HealthCheck;
import com.example.health.test.Test;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("custom")
public class CustomProperties {
    private HealthCheck healthCheck;
    private Test test;
}
