---
name: config-convention
description: 모듈의 Spring 설정, ConfigurationProperties와 Bean 구성 규칙을 정의한다.
---

# Config 코드 컨벤션

## 책임

`internal/config`는 모듈 실행에 필요한 Spring 설정과 Bean 구성을 둔다.

- 설정값을 객체로 바인딩한다.
- 설정값을 사용하는 Bean을 구성한다.
- 실행 환경에 따른 구현체를 선택한다.
- 모듈의 기술 설정을 구성한다.

도메인 규칙, 유스케이스 흐름, 외부 시스템 호출 로직은 `config`에 두지 않는다.

## 구조

```text
{module}/internal/config/
├─ {Purpose}Properties.java
├─ {Purpose}Config.java
└─ ...
```

설정 목적이 다른 경우 설정 객체와 Config 클래스를 분리한다. 모든 설정을 하나의 Config 클래스에 모으지 않는다.

## 설정 객체

여러 설정값을 하나의 목적이나 접두사로 묶어 사용해야 하는 경우 `@ConfigurationProperties`를 사용한다.

```java
@Validated
@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(
    @Min(100) int timeout
) {
}
```

- 설정 객체는 불변 객체로 작성한다.
- 설정 접두사는 설정 목적을 나타내야 한다.
- 설정값 검증이 필요한 경우 Bean 생성 시점에 검증되도록 한다.
- 설정 객체는 설정값을 전달하는 책임만 가진다.
- 도메인 규칙이나 환경별 분기 로직을 설정 객체에 넣지 않는다.

단일 값 하나를 주입하는 경우를 제외하고, 설정값을 사용하는 코드에서 `@Value`를 반복적으로 사용하지 않는다. 관련 설정값이 여러 개라면 `@ConfigurationProperties`로 묶는다.

## Bean 구성

설정값을 사용해 Bean을 생성하거나 Spring의 기술 설정을 등록하는 경우 `@Configuration`을 사용한다.

```java
@Configuration
class PaymentConfig {

    @Bean
    PaymentClient paymentClient(PaymentProperties properties) {
        return new PaymentClient(properties.timeout());
    }
}
```

- Bean 생성에 필요한 설정은 설정 객체를 주입받는다.
- `@Bean` 메서드 안에서 환경변수를 직접 조회하지 않는다.
- 같은 모듈에서 사용하는 기술 Bean은 해당 모듈의 `internal/config`에 둔다.
- 공통 Bean은 특정 도메인 모듈에 종속되지 않는 경우에만 전역 Config로 분리한다.
- Bean 생성 메서드에 유스케이스나 도메인 비즈니스 로직을 넣지 않는다.

## Profile 기반 구성

실행 환경에 따라 Bean 구현을 분리해야 하는 경우에만 `@Profile`을 사용한다.

```java
@Configuration
class PaymentConfig {

    @Bean
    @Profile("test")
    PaymentClient testPaymentClient() {
        return new TestPaymentClient();
    }

    @Bean
    @Profile("prod")
    PaymentClient productionPaymentClient() {
        return new ProductionPaymentClient();
    }
}
```

- Profile은 동일한 역할의 Bean 구현을 실행 환경별로 구분해야 할 때 사용한다.
- Profile 이름은 프로젝트에서 정한 실행 환경 이름을 사용한다.
- 서로 대체 관계인 구현체가 동시에 등록되지 않도록 Profile 조건을 명확히 한다.
- 단순한 설정값 차이는 Profile별 Bean보다 설정값 바인딩으로 해결한다.

## 배치 원칙

- 모듈 전용 설정은 해당 모듈의 `internal/config`에 둔다.
- 모듈 간 공유가 필요한 설정은 `global/config`에 둔다.
- 설정 클래스에서 다른 모듈의 `internal` 구현을 직접 조합하지 않는다.
- 설정 이름과 설정 객체의 구조는 사용하는 기술 또는 기능의 목적을 반영한다.
