# Workflow 문서 탐색 가이드

## 목적

자연어 요청에 맞는 Workflow를 선택하고, 작업에 필요한 문서만 순서대로 확인한다.
관련 없는 문서와 전체 코드베이스를 무차별적으로 읽지 않는다.

## 공통 탐색 순서

모든 작업은 다음 순서로 시작한다.

```text
AGENTS.md
    ↓
docs/boundary/boundary.md
    ↓
선택한 Workflow 문서
    ↓
작업 대상에 직접 관련된 문서
    ↓
관련 코드와 테스트
```

- `AGENTS.md`에서 프로젝트의 전체 작업 원칙과 문서 체계를 확인한다.
- `boundary.md`에서 작업 권한과 승인 조건을 확인한다.
- 선택한 Workflow에서 실행 절차와 완료 조건을 확인한다.
- 작업 대상에 직접 관련된 문서만 추가로 읽는다.
- 문서 확인 후 관련 코드와 테스트를 탐색한다.

## Workflow별 문서 접근 순서

### 요구사항 검증

```text
AGENTS.md
    ↓
boundary.md
    ↓
requirement.md
    ↓
관련 docs/domain/*.md
    ↓
필요한 architecture 문서
    ↓
관련 코드와 테스트
```

### 구현

```text
AGENTS.md
    ↓
boundary.md
    ↓
implement.md
    ↓
관련 docs/domain/*.md
    ↓
관련 architecture 컨벤션
    ↓
testing/test.md
    ↓
관련 코드와 테스트
```

### 디버깅

```text
AGENTS.md
    ↓
boundary.md
    ↓
debug.md
    ↓
관련 docs/domain/*.md
    ↓
관련 architecture 문서
    ↓
testing/test.md
    ↓
오류 경로의 코드와 테스트
```

### 리팩터링

```text
AGENTS.md
    ↓
boundary.md
    ↓
refactoring.md
    ↓
관련 architecture 문서
    ↓
관련 docs/domain/*.md
    ↓
testing/test.md
    ↓
대상 코드와 테스트
```

### 코드 리뷰

```text
AGENTS.md
    ↓
boundary.md
    ↓
review.md
    ↓
관련 docs/domain/*.md
    ↓
관련 architecture 문서
    ↓
testing/test.md
    ↓
대상 코드와 테스트
```

### 문서 동기화

```text
AGENTS.md
    ↓
boundary.md
    ↓
docs.md
    ↓
불일치가 의심되는 관련 문서
    ↓
관련 코드와 테스트
```

## Architecture 문서 선택 기준

전체 Architecture 문서를 읽지 않고 작업 대상에 따라 필요한 문서만 선택한다.

| 작업 대상 | 확인할 문서 |
| --- | --- |
| 패키지 구조 | `architecture/architecture-directory.md` |
| 모듈 경계·모듈 간 호출 | `architecture/conventions/spring-modulith.md`, `architecture/conventions/api/api.md` |
| Application | `architecture/conventions/internal/application/application.md` |
| Domain 객체 | `architecture/conventions/internal/domain/domain.md` |
| Presentation | `architecture/conventions/internal/presentation/presentation.md` |
| Infra 연동 | `architecture/conventions/internal/infra/infra.md` |
| Persist·Repository | `architecture/conventions/internal/persist/persist.md` |
| Config·Bean | `architecture/conventions/internal/config/config.md` |
| 예외 처리 | `architecture/conventions/global/exception.md` |

## 선택 규칙

- 작업 대상과 직접 관련 없는 도메인 문서는 읽지 않는다.
- 다른 Aggregate나 모듈과 협력하는 경우에만 해당 문서를 추가로 확인한다.
- 코드 변경이 없으면 구현·테스트 관련 문서를 불필요하게 확장해 읽지 않는다.
- 문서 간 충돌을 발견하면 임의로 정답을 정하지 않고 사용자에게 확인한다.
- Workflow의 완료 조건과 Boundary의 승인 조건을 충족하기 전에는 작업을 완료로 보고하지 않는다.
