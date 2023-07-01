package com.example.health.healthcheck;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HealthCheck {
    private boolean enabled;
    private String servletUri;
}

