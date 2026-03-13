package com.system.batch.config.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SystemJsonDeathJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ObjectMapper objectMapper;

    public SystemJsonDeathJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager, ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Job systemJsonDeathJob(Step systemJsonDeathStep) {
        return new JobBuilder("systemJsonDeathJob", jobRepository)
                .start(systemJsonDeathStep)
                .build();
    }

    @Bean
    public Step systemJsonDeathStep(
            FlatFileItemReader<SystemDeath> systemJsonDeathReader
    ) {
        return new StepBuilder("systemJsonDeathStep", jobRepository)
                .<SystemDeath, SystemDeath>chunk(10, transactionManager)
                .reader(systemJsonDeathReader)
                .writer(items -> items.forEach(System.out::println))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<SystemDeath> systemDeathReader(
        @Value("#{jobParameters['inputFile']}") String inputFile
    ) {
        return new FlatFileItemReaderBuilder<SystemDeath>()
                .name("systemDeathReader")
                .resource(new FileSystemResource(inputFile))
                .lineMapper((line, lineNumber) -> objectMapper.readValue(line, SystemDeath.class))
                .build();
    }

    public record SystemDeath(String command, int cpu, String status) {}
}
