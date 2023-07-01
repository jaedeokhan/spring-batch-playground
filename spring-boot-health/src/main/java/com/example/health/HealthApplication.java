package com.example.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
public class HealthApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthApplication.class, args);
	}

}
