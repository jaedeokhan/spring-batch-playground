package com.system.batch.config.file.writer.formatter;

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
import org.springframework.batch.item.file.transform.RecordFieldExtractor;
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
public class FormatterDeathNoteWriteJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job formatterDeathNoteWriteJob(Step formatterDeathNoteWriteStep) {
        return new JobBuilder("formatterDeathNoteWriteJob", jobRepository)
                .start(formatterDeathNoteWriteStep)
                .build();
    }

    @Bean
    public Step formatterDeathNoteWriteStep(
            ListItemReader<DeathNote> formatterDeathNoteListReader,
            FlatFileItemWriter<DeathNote> formatterDeathNoteWriter
    ) {
        return new StepBuilder("formatterDeathNoteWriteStep", jobRepository)
                .<DeathNote, DeathNote>chunk(10, transactionManager)
                .reader(formatterDeathNoteListReader)
                .writer(formatterDeathNoteWriter)
                .build();
    }

    @Bean
    public ListItemReader<DeathNote> formatterDeathNoteListReader() {
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
    public FlatFileItemWriter<DeathNote> formatterDeathNoteWriter(
            @Value("#{jobParameters['outputDir']}") String outputDir
    ) {
        return new FlatFileItemWriterBuilder<DeathNote>()
                .name("formatterDeathNoteWriter")
                .resource(new FileSystemResource(outputDir + "/death_note_report.txt"))
                .formatted()
                .format("처형 ID: %s | 처형일자: %s | 피해자: %s | 사인: %s")
                .sourceType(DeathNote.class)
                .names("victimId", "victimName", "executionDate", "causeOfDeath")
                .headerCallback(writer -> writer.write("===================== 처형 기록부 ======================="))
                .footerCallback(writer -> writer.write("===================== 처형 완료 ======================="))
//                .shouldDeleteIfExists(false)
//                .append(true)
//                .shouldDeleteIfEmpty(true)
//                .transactional(false)
//                .forceSync(true)
                .build();
    }

    public record DeathNote(
        String victimId,
        String victimName,
        String executionDate,
        String causeOfDeath
    ) {}
}
