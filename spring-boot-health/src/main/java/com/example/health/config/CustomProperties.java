package com.example.health.config;

import com.example.health.healthcheck.HealthCheck;
import com.example.health.test.Test;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@ConfigurationProperties("custom")
@Validated
public class CustomProperties {

    @NotNull
    private HealthCheck healthCheck;

    @NotNull
    private Test test;
}
