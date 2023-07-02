package com.example.health.config;

import com.example.health.config.CustomProperties;
import com.example.health.test.TestServlet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(
        value = "custom.test.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class TestServletConfiguration {

    @Autowired
    private CustomProperties properties;

    @Bean
    public ServletRegistrationBean<TestServlet> testServletRegistrationBean() {
        log.info("test.servlet-uri : {}", properties.getTest().getServletUri());
        return new ServletRegistrationBean<>(new TestServlet(), properties.getTest().getServletUri());
    }
}
