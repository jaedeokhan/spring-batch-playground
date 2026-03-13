package com.system.batch.config.json;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SystemJsonWriteJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job systemJsonWriteJob(Step systemJsonWriteStep) {
        return new JobBuilder("systemJsonWriteJob", jobRepository)
                .start(systemJsonWriteStep)
                .build();
    }

    @Bean
    public Step systemJsonWriteStep(
            ListItemReader<DeathNote> deathNoteListReader,
            JsonFileItemWriter<DeathNote> systemJsonWriter
    ) {
        return new StepBuilder("systemJsonWriteStep", jobRepository)
                .<DeathNote, DeathNote>chunk(10, transactionManager)
                .reader(deathNoteListReader)
                .writer(systemJsonWriter)
                .build();
    }

    @Bean
    public ListItemReader<DeathNote> systemJsonWriteReader() {
        List<DeathNote> victims = List.of(
                new DeathNote(
                        "KILL-001",
                        "김배치",
                        "2024-01-25",
                        "CPU 과부하"),
                new DeathNote(
                        "KILL-002",
                        "사불링",
                        "2024-01-26",
                        "JVM 스택오버플로우"),
                new DeathNote(
                        "KILL-003",
                        "박탐묘",
                        "2024-01-27",
                        "힙 메모리 고갈")
        );
        return new ListItemReader<>(victims);
    }

    @Bean
    @StepScope
    public JsonFileItemWriter<DeathNote> systemJsonWriter(
            @Value("#{jobParameters['outputDir']}") String outputDir
    ) {
        return new JsonFileItemWriterBuilder<DeathNote>()
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .resource(new FileSystemResource(outputDir + "/system_death_notes.json"))
                .name("systemJsonWriter")
                .build();
    }

    public record DeathNote(
        String victimId,
        String victimName,
        String executionDate,
        String causeOfDeath
    ) {}
}
