package com.system.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class SystemTerminatorPojoConfig {
    @Bean
    public Job processTerminatorPojoJob(JobRepository jobRepository, Step terminatorPojoStep) {
        return new JobBuilder("processTerminatorPojoJob", jobRepository)
                .start(terminatorPojoStep)
                .build();
    }

    @Bean
    public Step terminatorPojoStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        Tasklet terminatorPojoTasklet) {
        return new StepBuilder("terminatorPojoStep", jobRepository)
                .tasklet(terminatorPojoTasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet terminatorPojoTasklet(SystemInfiltrationParameters infiltrationParams) {
        return (contribution, chunkContext) -> {
            log.info("⚔️ 시스템 침투 작전 초기화!");
            log.info("임무 코드네임: {}", infiltrationParams.getMissionName());
            log.info("보안 레벨: {}", infiltrationParams.getSecurityLevel());
            log.info("작전 지휘관: {}", infiltrationParams.getOperationCommander());

            // 보안 레벨에 따른 침투 난이도 계산
            int baseInfiltrationTime = 60; // 기본 침투 시간 (분)
            int infiltrationMultiplier = switch (infiltrationParams.getSecurityLevel()) {
                case 1 -> 1; // 저보안
                case 2 -> 2; // 중보안
                case 3 -> 4; // 고보안
                case 4 -> 8; // 최고 보안
                default -> 1;
            };

            int totalInfiltrationTime = baseInfiltrationTime * infiltrationMultiplier;

            log.info("💥 시스템 해킹 난이도 분석 중...");
            log.info("🕒 예상 침투 시간: {}분", totalInfiltrationTime);
            log.info("🏆 시스템 장악 준비 완료!");

            return RepeatStatus.FINISHED;
        };
    }
}
