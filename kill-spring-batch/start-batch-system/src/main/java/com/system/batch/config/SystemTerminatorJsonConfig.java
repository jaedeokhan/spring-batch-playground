package com.system.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.converter.JsonJobParametersConverter;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class SystemTerminatorJsonConfig {

    @Profile("json")
    @Bean
    public JobParametersConverter jobParametersConverter() {
        return new JsonJobParametersConverter();
    }

    @Bean
    public Job processTerminatorJsonJob(JobRepository jobRepository, Step terminatorJsonStep) {
        return new JobBuilder("processTerminatorJsonJob", jobRepository)
                .start(terminatorJsonStep)
                .build();
    }

    @Bean
    public Step terminatorJsonStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        Tasklet terminatorJsonTasklet) {
        return new StepBuilder("terminatorJsonStep", jobRepository)
                .tasklet(terminatorJsonTasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet terminatorJsonTasklet(
            @Value("#{jobParameters['infiltrationTargets']}") String infiltrationTargets
    ) {
        return (contribution, chunkContext) -> {
            String[] targets = infiltrationTargets.split(",");

            log.info("⚡ 침투 작전 개시");
            log.info("첫 번째 타겟: {} 침투 시작", targets[0]);
            log.info("마지막 타겟: {} 에서 집결", targets[1]);
            log.info("🎯 임무 전달 완료");

            return RepeatStatus.FINISHED;
        };
    }
}
