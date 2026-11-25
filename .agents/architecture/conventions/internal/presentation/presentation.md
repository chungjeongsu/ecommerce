---
name: presentation-convention
description: Controller와 실행 진입점, HTTP 요청·응답 및 Usecase 연결 규칙을 정의한다.
---

# Presentation 코드 컨벤션

## 책임

`internal/presentation`은 외부 입력을 애플리케이션 Usecase 실행으로 연결하는 진입점이다.

- HTTP 요청을 처리한다.
- Scheduler, Listener 등 새로운 실행 흐름의 진입점을 둔다.
- 외부 Request를 Application Command 또는 Usecase 입력으로 변환한다.
- Usecase Result를 외부 Response로 변환한다.

Presentation은 유스케이스를 호출하는 역할만 담당한다. 도메인 규칙, 트랜잭션, Repository 조회와 저장을 직접 구현하지 않는다.

## 구조

```text
{module}/internal/presentation/
├─ controller/
├─ request/
├─ response/
├─ scheduler/
├─ listener/
└─ ...
```

실제 실행 흐름이 있을 때만 해당 패키지를 생성한다. 모든 모듈에 모든 진입점 패키지를 형식적으로 만들지 않는다.

## Controller

- HTTP 요청을 받아 Usecase를 호출한다.
- 경로 변수, 쿼리 파라미터, 요청 Body를 Request 모델로 받는다.
- Request 모델을 Application Command 또는 Usecase 입력으로 변환한다.
- Usecase Result를 Response 모델로 변환한다.
- HTTP 상태 코드와 외부 응답 형식을 결정한다.
- 도메인 Entity나 Persist Entity를 직접 반환하지 않는다.

```text
HTTP Request
    ↓
    Req
    ↓
Command
    ↓
Usecase
    ↓
    Result
    ↓
    Res
    ↓
HTTP Response
```

## Request와 Response

```text
request/
└─ {ActionName}Req.java

response/
└─ {ActionName}Res.java
```

- Request 모델은 외부 입력을 표현한다.
- Response 모델은 외부 출력 형식을 표현한다.
- Request는 Application Command로 변환한 뒤 Usecase에 전달한다.
- Usecase Result는 Response로 변환한 뒤 외부에 반환한다.
- 요청 DTO의 기본 형식 검증은 Presentation에서 수행한다.
- 도메인 불변식과 유스케이스 조건은 Domain과 Application에서 수행한다.
- Request와 Response는 Domain Entity, Persist Entity, Infra DTO를 재사용하지 않는다.
- Request/Response 명명은 `{ActionName}Req`, `{ActionName}Res` 형식을 사용한다.

## 새로운 실행 흐름

Controller 외에 Usecase를 호출하는 실행 흐름이 추가되면 해당 진입점도 Presentation에 둔다.

예시는 다음과 같다.

- `scheduler`: 정해진 시간이나 주기로 Usecase를 호출한다.
- `listener`: 메시지나 애플리케이션 신호를 받아 Usecase를 호출한다.
- `consumer`: 외부 메시지 입력을 받아 Usecase를 호출한다.

새로운 실행 흐름은 다음 규칙을 따른다.

- 실행 진입점은 Usecase를 호출한다.
- 실행 진입점에 도메인 규칙과 유스케이스 흐름을 직접 작성하지 않는다.
- 입력이 있는 경우 해당 실행 흐름에 맞는 입력 모델을 Application 입력으로 변환한다.
- 실행 방식이 달라도 동일한 Usecase를 재사용한다.
- 실행 흐름의 기술적 예외 처리와 재시도 정책은 해당 진입점의 책임으로 분리한다.

```text
Scheduler / Listener / Consumer
              ↓
           Usecase
              ↓
           Domain
```

## 계층 경계

- Presentation은 Application Usecase에 의존한다.
- Presentation은 Domain, Persist, Infra를 직접 호출하지 않는다.
- Controller에서 Repository를 직접 호출하지 않는다.
- Presentation 모델을 Application, Domain, Persist의 공용 모델로 사용하지 않는다.
