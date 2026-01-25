package com.system.batch.config.file.writer.delimited;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
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
public class DeathNoteWriteJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job deathNoteWriteJob(Step deathNoteWriteStep) {
        return new JobBuilder("deathNoteWriteJob", jobRepository)
                .start(deathNoteWriteStep)
                .build();
    }

    @Bean
    public Step deathNoteWriteStep(
            ListItemReader<DeathNote> deathNoteListReader,
            FlatFileItemWriter<DeathNote> deathNoteWriter
    ) {
        return new StepBuilder("deathNoteWriteStep", jobRepository)
                .<DeathNote, DeathNote>chunk(10, transactionManager)
                .reader(deathNoteListReader)
                .writer(deathNoteWriter)
                .build();
    }

    @Bean
    public ListItemReader<DeathNote> deathNoteListReader() {
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
    public FlatFileItemWriter<DeathNote> deathNoteWriter(
            @Value("#{jobParameters['outputDir']}") String outputDir
    ) {
        return new FlatFileItemWriterBuilder<DeathNote>()
                .name("deathNoteWriter")
                .resource(new FileSystemResource(outputDir + "/death_notes.csv"))
                .delimited()
                .delimiter(",")
                .sourceType(DeathNote.class)
                .names("victimId", "victimName", "executionDate", "causeOfDeath")
                .headerCallback(writer -> writer.write("처형ID,피해자명,처형일자,사인"))
                .build();
    }


//    @Getter @Setter @ToString @AllArgsConstructor
//    public static class DeathNote {
//        private String victimId;
//        private String victimName;
//        private String executionDate;
//        private String causeOfDeath;
//    }

    public record DeathNote(
        String victimId,
        String victimName,
        String executionDate,
        String causeOfDeath
    ) {}
}
