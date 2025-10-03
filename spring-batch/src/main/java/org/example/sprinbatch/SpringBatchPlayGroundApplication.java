package org.example.sprinbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchProcessing
@SpringBootApplication
public class SpringBatchPlayGroundApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBatchPlayGroundApplication.class, args);
    }
}
