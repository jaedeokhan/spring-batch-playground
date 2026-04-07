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

## Chunk FaultTolerant(내결함성)
스프링 배치는 내결함성(FaultTolerance) 기능을 제공한다.
재시도(Retry)와 건너뛰기(Skip)로 `청크 지향 처리`에서만 사용 가능하다.
`테스크릿 지향 처리`는 Spring Batch 내결함성(FaultTolerance) 기능 지원 대상이 아니다.

태스크릿 지향 처리에서는 Tasklet.execute() 메서드 하나만 사용한다.
따라서 해당 메서드에서 try-catch를 사용해 발생 가능한 예외를 원하는 대로 처리 가능하다.

### RetryTemplate
RetryTemplate은 Spring Retry 프로젝트의 컴포넌트로, `작업이 실패하면 정해진 정책에 따라 다시 시도` 하는 컴포넌트이다.
- RetryTemplate.execute()
    - canRetry()?
	     - Y, retryCallabck
		 - N, recoveryCallback

- canRetry()는 재시도 가능여부 판단한다. 사전에 정해진 재시도 정책(RetryPolicy)을 기반으로 작업을 다시 시도해도 되는가를 결정한다.
- retryCllback()은 핵심 로직 실행이다. 재시도가 가능하다고 판단되면 retryCallback을 호출한다.
   - 핵심 비지니스 로직이 담겨있고, 중요한 점은 이 콜백이 재시도만을 위한 것이 아니라 최초 실행부터 재시도까지 모든 시도가 이 retryCallback을 통해 수행된다.
- recoveryCallback은 최후의 수단으로 더 이상 재시도는 불가능할 때 호출된다. 최후로 발생한 예외를 그대로 전파거나 대체 로직을 수행한다.


### RetryPolicy 
RetryPolicy는 재시도 정책을 사용해 재시도 가능 여부를 결정한다.
별도 설정이 없을 경우 Spring Batch의 내결함성 기능은 `SimpleRetryPolicy`라는 재시도 정책을 사용한다.
SimpleRetryPolicy는 다음의 두 조건을 바탕으로 재시도 가능 여부를 결정한다.
1. 발생한 예외가 사전에 지정된 예외 유형에 해당하는가.
2. 현재 재시도 횟수가 최대 허용 횟수를 초과하지 않았는가.

### ItemReader는 재시도 따위는 없다ㄷㄷ
ItemReader는 재시도 기능의 보호 대상이 아니다.
ItemReader에서 발생한 예외는 재시도되지 않는다.

이유는 Spring Batch는 mutable한 데이터소스로부터 데이터를 읽는 상황까지 고려했기 때문이다.
읽으면 데이터가 사라지는 데이터 소스인 메시지 큐(RabbitMQ, SQS 등)을 고려한 것이다.
이미 사라진 데이터를 다시 읽을 수 없기에 ItemReader는 재시도하지 않는다.
`Spring Batch 6` 부터는 메시지 큐를 사용하는 예외적인 경우를 위해서 제한하는 맞지 않다고 생각해서 ItemReader에서 재시도 기능을 제공한다.

ItemReader의 기본 규약은 `forward only` 방식이다.
즉, 데이터를 단방향으로만 순차적으로 읽어나가는 것이 기본 원칙이다.
과거로 되돌아가 아이템을 다시 읽는 것은 ItemReader의 기본 설계 원칙에 위배된다.

그러면 어떻게 매번 재시도마다 Input Chunk를 전달 할 수 있을까?

정답은 내결함성 기능의 청크 버퍼링이다.
스프링 배치에서는 내결함성 기능을 활성화한 경우 ItemReader가 읽어들인 Input Chunk를 별도로 저장해준다.
덕분에 재시도가 필요할 때는 ItemReader를 되감지 않고도 이미 읽어둔 Chunk를 그대로 재사용하여 처리할 수 있다.

스프링 배치에서 내결함성 기능을 활성화하기 위해서는 faultTolerant()를 호출해야한다.
내결함성 기능을 사용하겠다고 명시하는 것이다.

SimpleRetryPolicy에서 사용할 `재시도 대상 예외 - retry()`, `재시도 대상 지정 - retryLimit()`을 선언한다.
retryLimit은 첫 번째 retryCallback 호출 1번이 포함되기에 실제 허용 가능한 재시도 횟수는 항상 retryLimit - 1이다.

listener()는 재시도 과정을 모니터링 가능하게 RetryListener를 설정 가능하다.

```java
    @Bean
    public Step terminationRetryStep() {
        return new StepBuilder("terminationRetryStep", jobRepository)
                .<Scream, Scream>chunk(3, transactionManager)
                .reader(terminationRetryReader())
                .processor(terminationRetryProcessor())
                .writer(terminationRetryWriter())
                .faultTolerant()
                .retry(TerminationFailedException.class)
                .retryLimit(3)
                .listener(retryListener())
                .build();
    }
```

### Retry - ItemProcessor => 재시도 횟수 item별
ItemProcessor와 ItemWriter에서의 재시도는 다른 방식으로 동작한다.

ItemProcessor에서의 재시도는 `아이템 단위로 재시도 컨텍서트가 관리된다.`
스프링 배치에서는 각 item 별로 얼마나 재시도했는지 따로따로 기록한다.
청크 전체가 다시 처리되지만, 재시도 횟수는 아이템 단위로 개별 관리된다.

이미 성공한 아이템들을 매번 다시 process()를 호출하는건 비효율적이라고 생각이 들면? 방법이 있다.
processorNonTransactional()를 사용하면 ItemProcessor를 비트랜잭션 상태로 표시하여 한 번 처리된 아이템의 결과를 캐시에 저장하낟.
즉, 실패한 아이템들에 대해서만 process()를 탄다.

### Retry - ItemWriter => 재시도 횟수 Chunk
ItemWriter에서 예외 발생 시 재시도 - 청크 단위로 재시도 관리
1. ItemWriter에서 예외 발생 시 ItemProcessor부터 처리가 재개된다.
2. ItemProcessor에서와 달리, ItemWriter에서의 재시도 횟수는 청크 단위로 관리된다.

### 백오프 정책(BackOffPolicy)
Retry를 할때는 백오프 정책을 항상 사용해주는게 좋다!

### Skip(건너뛰기) 기능
Step에서 건너뛰기(Skip) 기능을 활성화하면 recoveryCallback을 통해 예외를 건너뛸 수 있게 된다.
재시도 함께 주로 많이 사용한다.
일시적인 오류는 재시도하고, 영구적인 오류는 건너뛰어 배치 작업의 안정성을 더욱 향상시킬 수 있다.

Skip은 시스템의 생존이 개별 레코드의 처리보다 중요할 때 사용하는 전략이다.

스프링 배치의 건너뛰기 기능은 `SkipPolicy`라는 전략 컴포넌트를 기반으로 작동한다.
SkipPolicy는 특정 예외가 발생했을 때 해당 아이템을 건너뛸지 여부를 결정하는 핵심 컴포넌트이다.
별도 설정을 하지 않으면 기본적으로 `LimitCheckingItemSkipPolicy`가 사용된다.

`LimiteCheckingItemSkipPolicy`는 앞서 살펴본 `SimpleRetryPolicy`와 유사한 메커니즘으로 동작한다.

- skip(): 메서드에 건너뛸 예외를 지정한다.
- skipLimit(): 스텝에서 허용할 건너뛰기 최대 횟수를 설정한다.

재시도는 아이템별(ItemProcessor) 또는 청크별(ItemWriter)로 관리되던 것과 달리,
건너뛰기에서는 스텝 전체에서 발생한 총 건너뛰기 횟수를 카운팅한다.
어떤 아이템 또는 청크에서 예외가 발생했는지와 무관하게, 스텝 내의 전체 건너뛰기 횟수가 제한을 초과하면 스텝은 실패한다.
`skipLimit`의 기본값은 10이다. 별도로 지정하지 않으면 스텝 내에서 최대 10번의 건너뛰기가 허용된다.

앞서 이야기한 것처럼 건너뛰기 기능을 활성화하면 recoveryCallback 내에 건너뛰기 로직이 구현된다고 했다.
recoveryCallback에서 건너뛰기 로직이 실행된다는 것은 다시 말해 더 이상 재시도할 수 없을 때 건너뛰기가 작동된다는 것이다.

### Skip - ItemProcessor
SkipPolicy가 건너뛰기 가능하다고 판단하면 null을 반환하고, 그렇지 않으면 건너뛸 수 없으면 예외를 던진다.
ItemProcessor에서 null을 반환하면 해당 데이터는 필터링되어 ItemWriter에 전달되지 않는다.
정상적인 필터링 과정에서 null 반환과 건너뛰기로 인한 null 반환은 결과적으로 ItemWriter에 해당 데이터가 전달되지 않는다는 점에서 동일하다.

다만 스프링 배치는 이 경우를 구분해서, 필터링된 아이템은 필터 카운트(filterCount), 건너뛰기 된 아이템은 스킵 카운트(skipCount)에 각각 기록한다.

덕분에 스텝 실행 결과를 추적할 때 정상적인 필터링과 오류로 인한 건너뛰기를 구분할 수 있다.

ItemProcessor에서는 예외 발생하면 해당 지점에서 롤백 발생 후 청크 처리를 재개한다.
스킵이 발생한 데이터는 recoveryCallback() 실행으로 skip 한다.
건너뛰기 횟수는 아이템별로 관리되는 것이 아니라 `스텝 전체에서 발생환 총 횟수`로 관리된다.

### Skip - ItemWriter
ItemWriter는 단일 아이템이 아니라 청크 단위로 동작하기에 ItemProcessor보다 다소 복잡하다.
`ItemWriter는 청크 단위로 쓰기를 수행하므로, 예외 발생 시 어떤 아이템이 문제인지 즉시 알 수 없다.`

ItemWriter에서 예외가 발생하면 먼저 재시도 가능 여부를 판단하고, `재시도가 불가능하다고 판단되면 스캔 모드로 돌입한다.`
스캔 모드는 문제가 발생한 정확한 아이템을 식별하고 그것만 건너뛸 수 있게 한다.

### 스캔 모드란?
`스캔 모드`는 청크 내 아이템을 하나씩 개별 처리하는 특수 모드다.
이 모드에서는 일반적인 청크 처리에서와 달리 동작이 변경된다.
스캔 모드에서는 1건씩 처리되며 커밋 간격이 1로 변경된다.


### 스캔 모드에서의 Skip 프로세스
1. 예외 발생 및 재시도 판단
	- ItemWriter에서 예외가 발생하면 스프링 배치는 재시도 가능 여부를 확인한다.
2. 스캔 모드 전환
	- 재시도 불가능하다고 판단되면 스캔 모드로 전환되고 청크 트랜잭션이 롤백된다. 하지만 내결함성 기능 덕분에 스텝은 포기하지 않는다.
3. 개별 아이템 처리
	- 스캔 모드에서 청크 내 모든 아이템을 하나씩 개별 처리한다. ItemProcessor -> ItemWriter -> 즉시 커밋을 각 아이템마다 반복
4. 문제 아이템 건너뛰기
	- 개별 처리 중 예외가 발생한 아이템은 SkipPolicy 판단을 거쳐 건너뛴다. 정상 아이템들은 개별적으로 쓰여(write)진다.
5. 스캔 모드 종료
	- 청크 내 모든 아이템에 대한 개별 처리가 완료되면 스캔 모드가 종료된다.

### 스캔 모드의 비용
스캔 모드의 경우 청크 사이즈가 1000건이면 스캔 모드에서는 1000건을 1건씩 반복하게 되기에 비용이 늘어난다.

## Skip - ItemReader
RetryTemplate을 사용했던 ItemProcessor와 ItemWriter에서는 건너뛰기 로직이 recoveryCallback에 정의되어 있다고 설명했다.
그렇다면 재시도 기능이 없는 ItemReader에서는 건너뛰기 로직이 어떻게 구현될까?

ItemReasder의 건너뛰기 메커니즘은 단순하다.
스프링 배치 Step은 read() 메서드 호출 중 예외가 발생하면 이 예외를 catch하고 SkipPolicy를 사용해 해당 예외가 건너뛰기 가능한지 판단하다. 건너뛰기 가능하다고 판단되면 예외를 무시하고 바로 다음 아이템 읽기를 진행한다.

중요한건 발생한 예외를 무시하고 바로 다음 읽기 시도를 수행하기 때문에 ItemProcessor나 ItemWriter와 달리 청크 트랜잭션 롤백이 발생하지 않는다.



