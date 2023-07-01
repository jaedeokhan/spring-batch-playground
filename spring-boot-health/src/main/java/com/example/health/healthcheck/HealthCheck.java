package com.example.health.healthcheck;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@AllArgsConstructor
@Validated
public class HealthCheck {

    @NotNull
    private boolean enabled;

    @NotBlank
    private String servletUri;
}

