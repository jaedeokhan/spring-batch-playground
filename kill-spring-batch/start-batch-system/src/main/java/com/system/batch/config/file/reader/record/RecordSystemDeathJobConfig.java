package com.system.batch.config.file.reader.record;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RecordSystemDeathJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job recordSystemDeathJob(Step recordSystemDeathStep) {
        return new JobBuilder("recordSystemDeathJob", jobRepository)
                .start(recordSystemDeathStep)
                .build();
    }

    @Bean
    public Step recordSystemDeathStep(
            FlatFileItemReader<SystemDeath> recordSystemDeathReader,
            ItemWriter<SystemDeath> recordSystemDeathWriter
    ) {
        return new StepBuilder("recordSystemDeathStep", jobRepository)
                .<SystemDeath, SystemDeath>chunk(10, transactionManager)
                .reader(recordSystemDeathReader)
                .writer(recordSystemDeathWriter)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<SystemDeath> recordSystemDeathReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        return new FlatFileItemReaderBuilder<SystemDeath>()
                .name("recordSystemDeathReader")
                .resource(new FileSystemResource(inputFile))
                .delimited()
                .names("command", "cpu", "status")
                .targetType(SystemDeath.class) // record 전달 시 BeanWrapperFieldSetMapper가 아닌 RecordFieldSetMapper가 동작
                .linesToSkip(1)
                .build();
    }

    @Bean
    public ItemWriter<SystemDeath> recordSystemDeathWriter() {
        return items -> {
            for (SystemDeath item : items) {
                log.info("{} : ", item);
            }
        };
    }

    public record SystemDeath(String command, int cpu, String status) {}
}
