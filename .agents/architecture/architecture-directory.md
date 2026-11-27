---
name: architecture-directory
description: 도메인 모듈의 최상위 패키지와 api·internal 계층별 디렉터리 구조 및 책임을 정의한다.
---

# 패키지 및 디렉터리 구조

## 목적

도메인 모듈을 최상위 패키지로 구분하고, 모듈별 디렉터리와 내부 계층을 일관되게 배치한다.

모듈 경계와 공개 영역은 [Spring Modulith 컨벤션](conventions/spring-modulith.md)에서 정의한다.

Settlement는 구현 보류 상태다. 기능을 시작할 때 동일한 모듈 구조를 적용한다.

## 전체 구조

```text
com.ecommerce.project/
├─ ProjectApplication.java
├─ product/
│  ├─ api/
│  └─ internal/
│     ├─ application/
│     │  ├─ usecase/
│     │  ├─ command/
│     │  ├─ result/
│     │  └─ implementation classes
│     ├─ domain/
│     │  └─ domain objects and abstractions
│     ├─ config/
│     │  └─ module configuration
│     ├─ exception/
│     │  └─ module error codes
│     ├─ persist/
│     │  └─ persistence implementations
│     ├─ infra/
│     │  ├─ request/
│     │  ├─ response/
│     │  └─ integrations
│     └─ presentation/
│        ├─ controller/
│        ├─ request/
│        └─ response/
├─ inventory/
├─ order/
├─ payment/
├─ user/
└─ global/
   ├─ config/
   └─ exception/
```

`inventory`, `order`, `payment`, `user`도 동일한 `api`/`internal` 구조를 따른다. 각 모듈은 실제 책임이 존재하는 패키지만 생성한다.

## 패키지 책임

### `api`

- 모듈 외부에 공개할 API 계약을 둔다.
- API의 세부 구조와 작성 규칙은 [`conventions/api/api.md`](conventions/api/api.md)에서 확인한다.

### `internal/application`

- 유스케이스 실행을 조합하고 애플리케이션 흐름을 관리한다.
- `usecase`에는 UseCase 계약을 인터페이스로 둔다.
- `command`에는 유스케이스 입력 모델을 둔다.
- `result`에는 유스케이스 결과 모델을 둔다.
- Application 구현 클래스는 `application`에 둔다.
- Application의 세부 규칙은 [`conventions/internal/application/application.md`](conventions/internal/application/application.md)에서 확인한다.

### `internal/domain`

- 도메인 객체와 도메인 규칙을 둔다.
- 도메인 불변식을 이 계층에서 보호한다.
- 외부 기술에 대한 의존을 최소화한다.
- Domain 객체의 세부 규칙은 [`conventions/internal/domain/domain.md`](conventions/internal/domain/domain.md)에서 확인한다.

### `internal/config`

- 모듈 내부 구성과 설정을 둔다.
- Config의 세부 규칙은 [`conventions/internal/config/config.md`](conventions/internal/config/config.md)에서 확인한다.

### `internal/persist`

- 저장과 조회를 위한 영속성 구현을 둔다.
- Repository 인터페이스와 그 구현을 둔다.
- 영속성 모델과 도메인 모델의 변환을 담당한다.
- 영속성 구현이 도메인 규칙을 대신하지 않도록 한다.
- Persist의 세부 규칙은 [`conventions/internal/persist/persist.md`](conventions/internal/persist/persist.md)에서 확인한다.
- 데이터베이스 스키마와 Flyway 마이그레이션 규칙은 [`conventions/flyway.md`](conventions/flyway.md)에서 확인한다.

### `internal/infra`

- 외부 API, 메시지, 캐시, 파일 시스템 등 외부 시스템 연동을 둔다.
- `request`와 `response`에는 외부 시스템 통신 모델을 둔다.
- 외부 시스템의 기술적 세부사항이 다른 계층으로 유출되지 않도록 한다.
- Infra의 세부 규칙은 [`conventions/internal/infra/infra.md`](conventions/internal/infra/infra.md)에서 확인한다.

### `internal/presentation`

- HTTP 진입점과 통신 모델을 둔다.
- `request`와 `response`에는 HTTP 통신 모델을 둔다.
- HTTP 입력과 애플리케이션 입력 사이의 변환을 담당한다.
- 도메인 객체를 외부 응답으로 직접 노출하지 않는다.
- Presentation의 세부 규칙은 [`conventions/internal/presentation/presentation.md`](conventions/internal/presentation/presentation.md)에서 확인한다.

### `global`

- 특정 도메인 모듈에 속하지 않는 전역 관심사를 둔다.
- 여러 모듈이 공유하는 전역 기반 기능을 둔다.
- 모듈 간 공유 설정은 `global/config`에 둔다.
- 공통 예외 처리는 `global/exception`에 둔다.
- 전역 설정은 [`conventions/internal/config/config.md`](conventions/internal/config/config.md)에서 확인한다.
- 전역 예외는 [`conventions/global/exception.md`](conventions/global/exception.md)에서 확인한다.

## 관련 규칙

- 모듈 경계와 모듈 간 의존성은 [`conventions/spring-modulith.md`](conventions/spring-modulith.md)에서 확인한다.
- 공개 API의 구조와 계약은 [`conventions/api/api.md`](conventions/api/api.md)에서 확인한다.
## Architecture Document Responsibility Contract

Architecture 문서는 단순 참고 자료가 아니라 다음 Workflow 노드에 판단 근거를 전달하는 정보 제공자다. 각 문서는 변경 범위에 맞는 결정 패키지를 만들어 전달해야 한다.

### Input Contract

- 확정된 요구사항 또는 구현 변경 범위
- 영향을 받는 모듈, 패키지, 의존성, 영속성, API 또는 설정 정보
- 이전 노드의 결정, 미해결 사항, 근거 경로

### Responsibilities

- 요청된 변경의 Architecture 규칙 소유자를 식별한다.
- 확정된 규칙, 현재 구현 사실, 미해결 결정, 충돌을 구분한다.
- 허용되는 의존 방향과 영향을 받는 패키지 경계를 정의한다.
- 규칙을 검증하기 위해 필요한 테스트와 증거를 정의한다.
- 문서 소유자나 규칙이 모호하면 작업을 중단한다.

### Output Contract

모든 Architecture 문서는 다음 정보를 전달해야 한다.

- 적용 규칙과 근거 문서의 섹션
- 영향을 받는 모듈, 패키지, 공개 계약, 의존성
- 구현 시 지켜야 할 경계
- 필요한 단위·통합·계약·스키마 검증
- 미해결 질문과 작업을 막는 충돌
- 다음 Workflow 노드와 전달해야 할 정보

### Verification Gate

- [ ] 변경의 문서 소유자를 식별했다.
- [ ] 관련 규칙 본문을 끝까지 확인했다.
- [ ] 기존 규칙과 현재 구현 사실을 구분했다.
- [ ] 패키지와 의존성 경계를 명시했다.
- [ ] 필요한 검증을 구체적인 테스트 또는 점검 항목으로 연결했다.
- [ ] 미해결 결정은 기록했으며 필요한 경우 구현을 중단한다.

### Failure Policy

검증할 수 없는 항목이 하나라도 있으면 구현 권고가 아니라 중단 상태의 handoff를 반환한다.

### Workflow Handoff

`code-requirement-analyze`, `tdd-implementation`, `flyway`, `code-review`는 Architecture 문서의 출력 패키지를 입력으로 사용해야 한다. 문서를 읽었지만 출력 패키지를 기록하지 않은 것은 Architecture Gate를 통과한 것으로 보지 않는다.
