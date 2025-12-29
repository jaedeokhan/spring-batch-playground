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

