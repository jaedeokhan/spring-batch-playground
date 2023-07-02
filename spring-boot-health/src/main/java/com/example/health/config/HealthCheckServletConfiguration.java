package com.example.health.config;

import com.example.health.config.CustomProperties;
import com.example.health.healthcheck.HealthCheckServlet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(
        value = "custom.health-check.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class HealthCheckServletConfiguration {

    @Autowired
    private CustomProperties properties;

    @Bean
    public ServletRegistrationBean<HealthCheckServlet> healthCHeckServletRegistrationBean() {
        log.info("health-check.servlet-uri : {}", properties.getHealthCheck().getServletUri());
        return new ServletRegistrationBean<>(new HealthCheckServlet(), properties.getHealthCheck().getServletUri());
    }
}
