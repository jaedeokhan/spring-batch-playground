package com.example.health.config;

public class CustomConfig {

    private CustomProperties properties;

    public CustomConfig(CustomProperties properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "CustomConfig{" +
                "properties=" + properties +
                '}';
    }
}
