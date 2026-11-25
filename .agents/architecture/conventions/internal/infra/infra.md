---
name: infra-convention
description: 외부 API와 시스템 연동, Client 및 외부 요청·응답 모델의 구성 규칙을 정의한다.
---

# Infra 코드 컨벤션

## 책임

`internal/infra`는 애플리케이션과 외부 시스템 사이의 통신을 담당한다.

- 외부 API, 메시지 브로커, 캐시, 파일 시스템 등 외부 시스템과 통신한다.
- 외부 시스템의 요청·응답 형식을 관리한다.
- 외부 시스템의 기술적 예외와 응답 형식을 애플리케이션에서 사용할 수 있는 형태로 변환한다.
- 외부 시스템 접근 방식이 Application과 Domain으로 유출되지 않도록 한다.

도메인 규칙과 유스케이스 흐름은 Infra에 두지 않는다.

## 구조

```text
{module}/internal/infra/
├─ request/
├─ response/
├─ client/
└─ ...
```

실제 연동 책임이 있을 때만 하위 패키지를 생성한다. 외부 시스템의 종류나 역할이 다르면 Client를 분리한다.

## Client

Client는 외부 시스템과 실제로 통신하는 책임을 가진다.

외부 시스템 연동은 역할을 나타내는 인터페이스와 실제 연동 구현체를 분리한다.

```text
PgClient
└─ TossPgClientImpl
```

- `PgClient`는 Application이 사용하는 외부 결제 기능의 계약을 정의한다.
- `TossPgClientImpl`은 Toss Payments API를 사용하는 구체 구현이다.
- 외부 시스템이 변경되면 구현체를 교체하고, Application은 인터페이스에 의존한다.

- 외부 시스템의 URL, HTTP Method, Header, 인증, 직렬화 방식을 관리한다.
- 외부 시스템 호출에 필요한 `Req`를 생성한다.
- 외부 시스템의 응답을 `Res`로 변환한다.
- 외부 시스템의 HTTP 상태 코드와 기술적 오류를 처리한다.
- Client 안에서 주문, 결제, 재고와 같은 도메인 규칙을 판단하지 않는다.

```text
Application
    ↓
Infra Client
    ├─ 외부 Req 생성
    ├─ 외부 시스템 호출
    ├─ 외부 Res 수신
    └─ Application에서 사용할 결과로 변환
```

## Request와 Response

```text
request/
└─ {ActionName}Req.java

response/
└─ {ActionName}Res.java
```

- `Req`는 외부 시스템에 전송하는 요청 모델이다.
- `Res`는 외부 시스템에서 수신하는 응답 모델이다.
- 외부 시스템별 Request/Response는 서로 공유하지 않는다.
- Presentation의 `Req`/`Res`, API의 `Req`/`Res`와 재사용하지 않는다.
- 외부 시스템의 JSON 구조와 이름을 Infra 바깥으로 노출하지 않는다.
- Request/Response 명명은 `{ActionName}Req`, `{ActionName}Res` 형식을 사용한다.

## Application과의 경계

Application은 외부 시스템의 통신 세부사항을 알지 않아야 한다.

```text
Application 입력
    ↓
Infra Client
    ↓
외부 Req
    ↓
외부 시스템
    ↓
외부 Res
    ↓
Application 결과
```

- Application의 Command/Result를 외부 Request/Response로 직접 재사용하지 않는다.
- 외부 Response를 Domain 객체나 Application Result로 변환한 뒤 반환한다.
- 외부 시스템이 반환한 오류를 애플리케이션에서 처리할 수 있는 예외나 결과로 변환한다.
- 외부 API의 재시도, 타임아웃, 인증, 연결 설정은 Infra의 책임이다.

## 외부 시스템별 격리

- 외부 시스템마다 Client와 통신 모델을 분리한다.
- 한 외부 시스템의 Request/Response를 다른 외부 시스템에서 재사용하지 않는다.
- 외부 시스템 변경이 다른 계층으로 전파되지 않도록 변환 경계를 유지한다.
- 동일한 외부 시스템을 여러 유스케이스가 사용하더라도 통신 방식은 Client에 캡슐화한다.

## 계층 경계

- Infra는 외부 시스템의 기술을 사용할 수 있다.
- Infra는 Domain Entity의 불변식을 대신하지 않는다.
- Infra는 Presentation Controller를 호출하지 않는다.
- Infra의 Request/Response는 Application, Domain, Persist의 공용 모델이 아니다.
- 외부 시스템 연동 결과를 사용하는 유스케이스 흐름은 Application이 조합한다.
