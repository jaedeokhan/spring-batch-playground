# Spring Batch PlayGround

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
