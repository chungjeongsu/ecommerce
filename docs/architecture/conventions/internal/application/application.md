# Application 컨벤션

## 목적

`application`은 유스케이스를 실행하고 도메인 및 다른 계층을 조합하는 영역이다.

Application은 도메인 규칙을 직접 소유하지 않는다. 유스케이스 실행에 필요한 흐름을 관리하고, 도메인 객체의 행위를 호출한다.

## 구조

```text
{module}/internal/application/
├─ {ActionName}UsecaseFacade.java # 필요한 경우에만 둔다
├─ {ActionName}UsecaseImpl.java   # Usecase 구현
├─ {ActionName}ApiFacade.java     # 필요한 경우에만 둔다
├─ {ActionName}ApiImpl.java       # API 구현
├─ usecase/
│  └─ {ActionName}Usecase.java
├─ command/
│  └─ {ActionName}Command.java
└─ result/
   └─ {ActionName}Result.java
```

구체적인 클래스 배치와 명명 규칙은 이 문서의 기준을 따른다. 실제 기능에 필요하지 않은 패키지나 클래스를 형식적으로 생성하지 않는다.

## Usecase

- 유스케이스의 입력, 실행, 결과를 표현하는 애플리케이션 경계다.
- Usecase 이름은 `{ActionName}Usecase` 형식을 사용한다.
- Usecase 인터페이스는 `usecase` 패키지에 둔다.

## Command

- Command는 Usecase 실행에 필요한 입력 모델이다.
- Command 이름은 `{ActionName}Command` 형식을 사용한다.
- 외부 Request를 UsecaseImpl에서 사용하기 쉬운 Command 구조로 변환한 뒤 Usecase에 전달한다.
- Command는 HTTP, 외부 시스템, 영속성 모델의 구조를 그대로 노출하지 않는다.
- Command는 입력 전달을 담당하며 핵심 도메인 규칙을 대신하지 않는다.

## Result

- Result는 Usecase 실행 결과를 표현하는 애플리케이션 결과 모델이다.
- Result 이름은 `{ActionName}Result` 형식을 사용한다.
- Result는 도메인 Entity나 영속성 모델을 외부에 직접 노출하지 않는다.
- 필요한 결과 정보만 담고, 특정 Presentation 또는 Infra 형식에 종속되지 않는다.

## Usecase 구현체

- `{ActionName}UsecaseImpl`은 `{ActionName}Usecase`의 구현체다.
- `{ActionName}ApiImpl`은 `api` 패키지의 `{ActionName}Api` 구현체다.
- Usecase와 API의 구현체는 모두 `application`에 둔다.
- Usecase 실행에 필요한 Domain, Persist, Infra 및 다른 모듈의 공개 API를 조합한다.
- 유스케이스의 실행 순서와 계층 간 변환을 관리한다.
- 도메인 객체의 상태 변경은 도메인 객체의 행위를 통해 수행한다.

## 트랜잭션

- 기본 트랜잭션 경계는 `{ActionName}UsecaseImpl`에서 관리한다.
- 하나의 유스케이스 실행에 필요한 작업은 유스케이스의 일관성을 기준으로 트랜잭션 범위를 결정한다.
- 트랜잭션을 Domain 객체나 Controller에 두지 않는다.
- 단순히 여러 작업을 호출한다는 이유만으로 트랜잭션을 분리하지 않는다.

## Facade

`Impl`만으로 API 또는 Usecase의 트랜잭션을 여러 경계로 분리하기 어렵거나, 같은 클래스 내부 호출로 인해 Spring AOP 프록시가 적용되지 않는 경우에만 Facade를 둔다. Facade는 기본 구조에 항상 포함하지 않는다.

- Usecase Facade 이름은 `{ActionName}UsecaseFacade` 형식을 사용한다.
- API Facade 이름은 `{ActionName}ApiFacade` 형식을 사용한다.
- Facade는 필요한 경우 별도 Spring Bean으로 분리된 진입점이자 트랜잭션 경계가 된다.
- Usecase Facade는 `{ActionName}UsecaseImpl`에 위임한다.
- API Facade는 `{ActionName}ApiImpl`에 위임한다.
- Facade는 트랜잭션 분리 또는 프록시 적용 문제를 해결할 필요가 있을 때만 사용한다.
- 자기 자신을 호출하는 방식으로 `@Transactional` 적용을 기대하지 않는다.

```text
API 또는 호출자
  ↓
{ActionName}UsecaseFacade 또는 {ActionName}ApiFacade
  ↓
{ActionName}UsecaseImpl 또는 {ActionName}ApiImpl
  ↓
Domain / Persist / Infra / 다른 모듈 API
```

Application은 외부 통신 DTO의 세부 형식에 의존하지 않고 Command를 입력으로 받아 Result를 반환한다. Request를 Command로, Result를 Response로 변환하는 책임은 해당 진입점에 둔다.

- Presentation: HTTP Request를 Command로, Result를 HTTP Response로 변환한다.
- API 구현체: 공개 API Request를 Command로, Result를 공개 API Response로 변환한다.
- Infra Client: 외부 시스템의 Request/Response를 Application에서 사용할 결과로 변환한다.
