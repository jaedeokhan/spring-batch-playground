package com.system.batch.config.file.writer.multiresource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MultiResourceDeathNoteWriteJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job multiResourceDeathNoteWriteJob(Step multiResourceDeathNoteWriteStep) {
        return new JobBuilder("multiResourceDeathNoteWriteJob", jobRepository)
                .start(multiResourceDeathNoteWriteStep)
                .build();
    }

    @Bean
    public Step multiResourceDeathNoteWriteStep(
            ListItemReader<DeathNote> multiResourceDeathNoteListReader,
            MultiResourceItemWriter<DeathNote> multiResourceItemWriter
    ) {
        return new StepBuilder("multiResourceDeathNoteWriteStep", jobRepository)
                .<DeathNote, DeathNote>chunk(10, transactionManager)
                .reader(multiResourceDeathNoteListReader)
                .writer(multiResourceItemWriter)
                .build();
    }

    @Bean
    public ListItemReader<DeathNote> multiResourceDeathNoteListReader() {
        List<DeathNote> deathNotes = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            String id = String.format("KILL-%03d", i);
            LocalDate date = LocalDate.now().plusDays(i);
            deathNotes.add(new DeathNote(
                    id,
                    "피해자" + i,
                    date.format(DateTimeFormatter.ISO_DATE),
                    "처형사유" + i
            ));
        }
        return new ListItemReader<>(deathNotes);
    }

    @Bean
    @StepScope
    public MultiResourceItemWriter<DeathNote> multiResourceItemWriter(
            @Value("#{jobParameters['outputDir']}") String outputDir
    ) {
        return new MultiResourceItemWriterBuilder<DeathNote>()
                .name("multiResourceItemWriter")
                .resource(new FileSystemResource(outputDir + "/multi_resource_death_note"))
                .itemCountLimitPerResource(10)
                .delegate(delegateItemWriter())
                .resourceSuffixCreator(index -> String.format("_%03d.txt", index))
                .build();
    }

    @Bean
    public FlatFileItemWriter<DeathNote> delegateItemWriter(){
        return new FlatFileItemWriterBuilder<DeathNote>()
                .name("delegateItemWriter")
                .formatted()
                .format("처형 ID: %s | 처형일자: %s | 피해자: %s | 사인: %s")
                .sourceType(DeathNote.class)
                .names("victimId", "victimName", "executionDate", "causeOfDeath")
                .headerCallback(writer -> writer.write("===================== 처형 기록부 ======================="))
                .footerCallback(writer -> writer.write("===================== 처형 완료 ======================="))
                .build();
    }



    public record DeathNote(
        String victimId,
        String victimName,
        String executionDate,
        String causeOfDeath
    ) {}
}
