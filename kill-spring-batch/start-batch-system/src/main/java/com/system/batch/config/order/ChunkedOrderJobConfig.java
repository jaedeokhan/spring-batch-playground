package com.system.batch.config.order;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChunkedOrderJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job chunkedOrderJob(Step chunkedOrderStep) {
        return new JobBuilder("chunkedOrderJob", jobRepository)
                .start(chunkedOrderStep)
                .build();
    }

    @Bean
    public Step chunkedOrderStep(
            ItemReader<TestData> itemReader,
            ItemProcessor<TestData, TestData> itemProcessor,
            ItemWriter<TestData> itemWriter
    ) {
        return new StepBuilder("chunkedOrderStep", jobRepository)
                .<TestData, TestData>chunk(10, transactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean
    public ItemReader<TestData> itemReader() {
        return new ItemReader<>() {
            private int i = 0;

            @Override
            public TestData read() {
                if (i >= 25) return null; // 종료 조건

                TestData data = new TestData("READ-" + i);
                log.info("[READER ] {}", data);
                i++;
                return data;
            }
        };
    }

    @Bean
    public ItemProcessor<TestData, TestData> itemProcessor() {
        return item -> {
            log.info("[PROCESS] {}", item);
            return item; // 그대로 통과
        };
    }

    @Bean
    public ItemWriter<TestData> itemWriter() {
        return items -> {
            log.info("[WRITER ] chunk size={}", items.size());
            items.forEach(item -> log.info("         {}", item));
        };
    }

    @Getter @ToString @AllArgsConstructor
    public static class TestData {
        public String testData;
    }
}
