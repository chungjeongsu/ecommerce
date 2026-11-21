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
- `usecase`에는 유스케이스 계약을 인터페이스로 둔다.
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
