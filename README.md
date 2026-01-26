# Spring Batch PlayGround

## kill-spring-batch

### plain spring batch와 spring boot batch
plain spring batch에서는 기본적으로 DefaultBatchConfiguration을 상속해야하는 BatchConfig가 필요하다.
Batch Config에서는 데이터 소스 설정이 필요하다.
cmd, bash 커맨드에서 실행할 경우에는 mainClass 설정이 필요하다.

1. spring-batch-core 라이브러리 필요
2. DeafultBatchConfiguration 상속 및 데이터소스 구현 피요
3. 커맨드 실행 시 mainClass 지정 필요

```groovy
application {
    mainClass = 'org.springframework.batch.core.launch.support.CommandLineJobRunner'
}
```

커맨드 실행 방법은 하단과 같다.

```groovy
./gradlew run --args="com.system.batch.config.SystemTerminationConfig systemTerminationSimulationJob"
```

spring boot batch에서는 별도 DefaultBatchConfiguration 상속은 필요없다.
데이터 소스 설정도 필요없다.
spring-boot-starter-batch가 자동으로 처리해준다.

커맨드 실행 방법은 하단과 같고 plain과 다른점은 spring.batch.job.name 파라미터로 job 이름만 던져주면 된다.

```groovy
./gradlew bootRun --args='--spring.batch.job.name=systemTerminationSimulationJob'
```

## sec03-03 JobParameter
JobParameter의 실행 방법은 3가지가 있다.
1. 애플리케이션 실행 시 주입
2. 코드로 생성
3. SpEL 이용

### 애플리케이션 실행 시 주입

bash에서 실행하게 하려면 (, ) 앞에는 역슬래시(\)가 필요
seq나 date 뒤에 (long), (date) 데이터 타입을 적어주지 않으면 실행 시 컨버터 에러가 발생

```bash
java -jar spring-batch-playground-1.0-SNAPSHOT.jar name=user2 seq\(long\)=3L date\(date\)=2024/06/18 age\(double\)=17.5
```

## 5. 스프링 배치 두 가지 스텝 유형
청크 지향 처리에 시퀀스 다이어그램에 대한 오해가 있었다.
https://github.com/spring-projects/spring-batch/commit/3fbfbb95033c228a02d03c90d2bf0fe566b4e5f5
chunk가 10개일 때 read->process가 순차적으로 10번 호출되는줄 알고 있었지만,
사실은 read 10번 수행, process 10번 수행 후 write()에 던져주는 것이다..

잘못된 흐름 : read() -> process() -> read() -> process -> ... write(lists) 
정상 흐름 : read() -> read() ... -> process() -> process -> ... wirte(lists)

### 청크 단위 반복의 끝은 어디인가?
Chunk가 모든 데이터의 끝이라는 판단은 어떻게 할까?
ItemREader의 read() 메서드가 null을 반환할 때 Spring Batch가 모든 데이터를 읽었다고 인식하는 신호읻.

### 테스크릿과 청크 지향 처리의 트랜잭션
테스크릿 트랜잭션은 execute() 메서드 전 시작하고 종료하면 커밋
청크 지향 트랜잭션은 Chunk 크기만큼 반영

### 적절한 청크 사이즈란?
정답은 없고, 트레이드오프와 비즈니스 요구사항, 처리할 데이터 양을 고려해서 적절하게 선택해야 한다.

#### 청크 사이즈가 클 때
메모리에 많은 데이터 로드, 트랜잭션 경계가 커지므로 문제 발생 시 롤백되는 데이터 양도 커진다.

#### 청크 사이즈가 작을 때
트랜잭션 경계가 작아져서 문제 발생시 롤백 데이터 최소화, 그러나 읽기/쓰기 I/O가 자주 발생된다.

## JobParameters

배치 작업에 전달되는 입력값, 배치 잡을 유연하고 동적으로 사용하게 해주는 입력 파라미터이다.

## 프로퍼티와 JobParameters의 차이점

프로퍼티인(`-D`)의 경우에는 프로그램 로딩시에 한 번 가져가는 설정이다.

웹 요청으로 들어올 때마다 비동기로 배치 Job을 실행하는 온라인 배치 앱이 있다면 프로퍼티로는 불가능하다.

### JobParameters 기본 문법

`parameterName=parameterValue,parameterType,identificationFlag`

Ex) inputFilePath=/data/users.csv,java.lang.String

- identificationFlag: Spring Batch에게 해당 파라미터가 JobInstance 식별에 사용될 파라미터인지 여부를 전달하는 값으로 true이면 식별에 사용된다는 의미

### JobParameters 구현체

[DefaultJobParametersConverter](https://docs.spring.io/spring-batch/docs/current/api/org/springframework/batch/core/converter/DefaultJobParametersConverter.html)와 [DefaultConversionService](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/convert/support/DefaultConversionService.html)의 javadoc을 참고

### 기본적인 String, Integer 파라미터 실행방법

```bash
    @Bean
    @StepScope
    public Tasklet terminatorTasklet(
            @Value("#{jobParameters['terminatorId']}") String terminatorId,
            @Value("#{jobParameters['targetCount']}") Integer targetCount
    ) {
        return (contribution, chunkContext) -> {
            log.info("시스템 종결자 정보:");
            log.info("ID: {}", terminatorId);
            log.info("제거 대상 수: {}", targetCount);
            log.info("⚡ SYSTEM TERMINATOR {} 작전을 개시합니다.", terminatorId);
            log.info("☠️ {}개의 프로세스를 종료합니다.", targetCount);

            for (int i = 1; i <= targetCount; i++) {
                log.info("💀 프로세스 {} 종료 완료!", i);
            }

            log.info("🎯 임무 완료: 모든 대상 프로세스가 종료되었습니다.");
            return RepeatStatus.FINISHED;
        };
    }
```

```bash
./gradlew bootRun --args='--spring.batch.job.name=processTerminatorWithParamJob terminatorId=KILL-9,java.lang.String targetCount=5,java.lang.Integer
```

### LocalDate와 LocalDateTime 파라미터 실행방법

- 한 가지 주의점은 날짜 타입의 경우에는 ISO 표준 형식으로 전달해야 한다.
    - [java.util.Date](http://java.util.Date) → ISO_INSTANT
    - java.time.LocalTime → ISO_LOCAL_TIME

```bash
    @Bean
    @StepScope
    public Tasklet terminatorWithParamTasklet(
            @Value("#{jobParameters['executionDate']}") LocalDate executionDate,
            @Value("#{jobParameters['startTime']}") LocalDateTime startTime
    ) {
        return (contribution, chunkContext) -> {
            log.info("시스템 처형 정보");
            log.info("처형 예정일: {}", executionDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));
            log.info("작전 개시 시각: {}", startTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초")));
            log.info("⚡ {}에 예정된 시스템 정리 작전을 개시합니다.", executionDate);
            log.info("💀 작전 시작 시각: {}", startTime);

            LocalDateTime currentTime = startTime;
            for (int i = 0; i <= 3; i++) {
                currentTime = currentTime.plusHours(1);
                log.info("☠️ 시스템 정리 {}시간 경과... 현재 시각:{}", i, currentTime.format(DateTimeFormatter.ofPattern("HH시 mm분")));
            }

            log.info("🎯 임무 완료: 모든 대상 시스템이 성공적으로 제거되었습니다.");
            log.info("⚡ 작전 종료 시각: {}", currentTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초")));

            return RepeatStatus.FINISHED;
        };
    }
```

```bash
./gradlew bootRun --args='--spring.batch.job.name=processTerminatorWithParamJob executionDate=2026-01-14,java.time.LocalDate startTime=2026-01-14T14:30:00,java.time.LocalDateTime'
```

### Enum 타입의 파라미터 실행방법

- Enum의 경우에는 해당 객체를 바로 사용하면 된다.

```bash
    @Bean
    @StepScope
    public Tasklet terminatorWithEnumParamTasklet(
            @Value("#{jobParameters['questDifficulty']}") QuestDifficulty questDifficulty
    ) {
        return (contribution, chunkContext) -> {
            log.info("⚔️ 시스템 침투 작전 개시!");
            log.info("임무 난이도: {}", questDifficulty);
            // 난이도에 따른 보상 계산
            int baseReward = 100;
            int rewardMultiplier = switch (questDifficulty) {
                case EASY -> 1;
                case NORMAL -> 2;
                case HARD -> 3;
                case EXTREME -> 5;
            };
            int totalReward = baseReward * rewardMultiplier;
            log.info("💥 시스템 해킹 진행 중...");
            log.info("🏆 시스템 장악 완료!");
            log.info("💰 획득한 시스템 리소스: {} 메가바이트", totalReward);
            return RepeatStatus.FINISHED;
        };
    }
```

```bash
./gradlew bootRun --args='--spring.batch.job.name=processTerminatorWithEnumParamJob questDifficulty=HARD,com.system.batch.QuestDifficulty'
```

## Chunked Diagram
Chunked의 흐름을 데이터가 10개일 때 read() -> process()가 10번 반복되서 수행되는줄 아는 사람이 많다.
실제로는 read() -> 10번 -> process() -> 10번 -> write()와 같이 수행된다.
https://github.com/spring-projects/spring-batch/commit/3fbfbb95033c228a02d03c90d2bf0fe566b4e5f5

```bash
./gradlew bootRun --args='--spring.batch.job.name=chunkedOrderJob'
```
