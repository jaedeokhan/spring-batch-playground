package com.system.batch.config.file.reader.pattern;

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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.BindException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PatternSystemLogJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job patternSystemLogJob(Step patternSystemLogStep) {
        return new JobBuilder("patternSystemLogJob", jobRepository)
                .start(patternSystemLogStep)
                .build();
    }

    @Bean
    public Step patternSystemLogStep(
        FlatFileItemReader<PatternSystemLog> patternSystemLogReader,
        ItemWriter<PatternSystemLog> patternSystemLogWriter
    ) {
        return new StepBuilder("patternSystemLogStep", jobRepository)
                .<PatternSystemLog, PatternSystemLog>chunk(10, transactionManager)
                .reader(patternSystemLogReader)
                .writer(patternSystemLogWriter)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<PatternSystemLog> patternSystemLogReader(
        @Value("#{jobParameters['inputFile']}") String inputFile
    ) {
        return new FlatFileItemReaderBuilder<PatternSystemLog>()
                .name("patternSystemLogReader")
                .resource(new FileSystemResource(inputFile))
                .lineMapper(patternSystemLogLineMapper())
                .build();
    }

    @Bean
    public PatternMatchingCompositeLineMapper<PatternSystemLog> patternSystemLogLineMapper() {
        PatternMatchingCompositeLineMapper<PatternSystemLog> lineMapper = new PatternMatchingCompositeLineMapper<>();

        Map<String, LineTokenizer> tokenizers = new HashMap<>();
        tokenizers.put("ERROR*", errorLineTokenizer());
        tokenizers.put("ABORT*", abortLineTokenizer());
        tokenizers.put("COLLECT*", collectLineTokenizer());
        lineMapper.setTokenizers(tokenizers);

        Map<String, FieldSetMapper<PatternSystemLog>> mappers = new HashMap<>();
        mappers.put("ERROR*", new ErrorFieldSetMapper());
        mappers.put("ABORT*", new AbortFieldSetMapper());
        mappers.put("COLLECT*", new CollectFieldSetMapper());
        lineMapper.setFieldSetMappers(mappers);

        return lineMapper;
    }

    @Bean
    public DelimitedLineTokenizer errorLineTokenizer() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(",");
        tokenizer.setNames("type", "application", "errorType", "timestamp", "message", "resourceUsage", "logPath");
        return tokenizer;
    }

    @Bean
    public DelimitedLineTokenizer abortLineTokenizer() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(",");
        tokenizer.setNames("type", "application", "errorType", "timestamp", "message", "exitCode", "processPath", "status");
        return tokenizer;
    }

    @Bean
    public DelimitedLineTokenizer collectLineTokenizer() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(",");
        tokenizer.setNames("type", "dumpType", "processId", "timestamp", "dumpPath");
        return tokenizer;
    }

    @Bean
    public ItemWriter<PatternSystemLog> patternSystemLogWriter() {
        return items -> {
            for (PatternSystemLog item : items) {
                log.info("{}", item);
            }
        };
    }

    @Getter @Setter @ToString
    public static class PatternSystemLog {
        private String type;
        private String timestamp;
    }

    @Getter @Setter @ToString(callSuper = true)
    public static class ErrorLog extends PatternSystemLog {
        private String application;
        private String errorType;
        private String message;
        private String resourceUsage;
        private String logPath;
    }

    @Getter @Setter @ToString(callSuper = true)
    public static class AbortLog extends PatternSystemLog {
        private String application;
        private String errorType;
        private String message;
        private String exitCode;
        private String processPath;
        private String status;
    }

    @Getter @Setter @ToString(callSuper = true)
    public static class CollectLog extends PatternSystemLog {
        private String dumpType;
        private String processId;
        private String dumpPath;
    }

    public static class ErrorFieldSetMapper implements FieldSetMapper<PatternSystemLog> {
        @Override
        public PatternSystemLog mapFieldSet(FieldSet fs) throws BindException {
            ErrorLog errorLog = new ErrorLog();
            errorLog.setType(fs.readString("type"));
            errorLog.setApplication(fs.readString("application"));
            errorLog.setErrorType(fs.readString("errorType"));
            errorLog.setTimestamp(fs.readString("timestamp"));
            errorLog.setMessage(fs.readString("message"));
            errorLog.setResourceUsage(fs.readString("resourceUsage"));
            errorLog.setLogPath(fs.readString("logPath"));
            return errorLog;
        }
    }

    public static class AbortFieldSetMapper implements FieldSetMapper<PatternSystemLog> {
        @Override
        public PatternSystemLog mapFieldSet(FieldSet fs) throws BindException {
            AbortLog abortLog = new AbortLog();
            abortLog.setType(fs.readString("type"));
            abortLog.setApplication(fs.readString("application"));
            abortLog.setErrorType(fs.readString("errorType"));
            abortLog.setTimestamp(fs.readString("timestamp"));
            abortLog.setMessage(fs.readString("message"));
            abortLog.setExitCode(fs.readString("exitCode"));
            abortLog.setProcessPath(fs.readString("processPath"));
            abortLog.setStatus(fs.readString("status"));
            return abortLog;
        }
    }

    public static class CollectFieldSetMapper implements FieldSetMapper<PatternSystemLog> {
        @Override
        public PatternSystemLog mapFieldSet(FieldSet fs) throws BindException {
            CollectLog collectLog = new CollectLog();
            collectLog.setType(fs.readString("type"));
            collectLog.setDumpType(fs.readString("dumpType"));
            collectLog.setProcessId(fs.readString("processId"));
            collectLog.setTimestamp(fs.readString("timestamp"));
            collectLog.setDumpPath(fs.readString("dumpPath"));
            return collectLog;
        }
    }
}
