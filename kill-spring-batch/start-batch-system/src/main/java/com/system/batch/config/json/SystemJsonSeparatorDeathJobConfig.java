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
import org.springframework.batch.item.file.separator.JsonRecordSeparatorPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SystemJsonSeparatorDeathJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ObjectMapper objectMapper;

    public SystemJsonSeparatorDeathJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager, ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Job systemJsonSeparatorDeathJob(Step systemJsonSeparatorDeathStep) {
        return new JobBuilder("systemJsonSeparatorDeathJob", jobRepository)
                .start(systemJsonSeparatorDeathStep)
                .build();
    }

    @Bean
    public Step systemJsonSeparatorDeathStep(
            FlatFileItemReader<SystemDeath> systemJsonSeparatorDeathReader
    ) {
        return new StepBuilder("systemJsonSeparatorDeathStep", jobRepository)
                .<SystemDeath, SystemDeath>chunk(10, transactionManager)
                .reader(systemJsonSeparatorDeathReader)
                .writer(items -> items.forEach(System.out::println))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<SystemDeath> systemJsonSeparatorDeathReader(
        @Value("#{jobParameters['inputFile']}") String inputFile
    ) {
        return new FlatFileItemReaderBuilder<SystemDeath>()
                .name("systemJsonSeparatorDeathReader")
                .resource(new FileSystemResource(inputFile))
                .lineMapper((line, lineNumber) -> objectMapper.readValue(line, SystemDeath.class))
                .recordSeparatorPolicy(new JsonRecordSeparatorPolicy())
                .build();
    }

    public record SystemDeath(String command, int cpu, String status) {}
}
