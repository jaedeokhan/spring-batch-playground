package com.system.batch.config.file.reader.multiresource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MultiResourceSystemFailureJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job multiResourceSystemFailureJob(Step multiResourceSystemFailureStep) {
        return new JobBuilder("multiResourceSystemFailureJob", jobRepository)
                .start(multiResourceSystemFailureStep)
                .build();
    }

    @Bean
    public Step multiResourceSystemFailureStep(
            MultiResourceItemReader<MultiResourceSystemFailure> multiResourceSystemFailureItemReader,
            MultiResourceSystemFailureStdoutItemWriter multiResourceSystemFailureStdoutItemWriter
            ) {
        return new StepBuilder("multiResourceSystemFailureStep", jobRepository)
                .<MultiResourceSystemFailure, MultiResourceSystemFailure>chunk(10, transactionManager)
                .reader(multiResourceSystemFailureItemReader)
                .writer(multiResourceSystemFailureStdoutItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public MultiResourceItemReader<MultiResourceSystemFailure> multiResourceSystemFailureItemReader(
            @Value("#{jobParameters['inputFilePath']}") String inputFilePath) {
        return new MultiResourceItemReaderBuilder<MultiResourceSystemFailure>()
                .name("multiResourceSystemFailureItemReader")
                .resources(new Resource[]{
                        new FileSystemResource(inputFilePath + "/critical-failures.csv"),
                        new FileSystemResource(inputFilePath + "/normal-failures.csv")
                })
                .delegate(multiResourceSystemFailureItemReaderImpl())
                .build();
    }

    @Bean
    public FlatFileItemReader<MultiResourceSystemFailure> multiResourceSystemFailureItemReaderImpl() {
        return new FlatFileItemReaderBuilder<MultiResourceSystemFailure>()
                .name("multiResourceSystemFailureItemReaderImpl")
                .delimited()
                .delimiter(",")
                .names("errorId", "errorDateTime", "severity", "processId", "errorMessage")
                .targetType(MultiResourceSystemFailure.class)
                .linesToSkip(1)
                .build();
    }

    @Bean
    public MultiResourceSystemFailureStdoutItemWriter multiResourceSystemFailureStdoutItemWriter() {
        return new MultiResourceSystemFailureStdoutItemWriter();
    }

    public static class MultiResourceSystemFailureStdoutItemWriter implements ItemWriter<MultiResourceSystemFailure> {
        @Override
        public void write(Chunk<? extends MultiResourceSystemFailure> chunk) throws Exception {
            for (MultiResourceSystemFailure failure : chunk) {
                log.info("Processing system failure : {}", failure);
            }
        }
    }

    @Getter @Setter @ToString
    public static class MultiResourceSystemFailure {
        private String errorId;
        private String errorDateTime;
        private String severity;
        private Integer processId;
        private String errorMessage;
    }

}
