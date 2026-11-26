---
name: feature-development
description: 전용 Git 작업 브랜치에서 기존 도메인 규칙을 유지한 새 기능을 분석하고 TDD로 구현·검토할 때 사용한다.
---

## Flow

```text
git-workflow (작업 브랜치 준비)
        ↓
requirement-analyze
        ↓
[규칙 문서 변경 시] docs-sync
        ↓
tdd-implementation
        ↓
[JPA Entity 변경 시] flyway
        ↓
code-review
        ↓
git-workflow (commit · push)
```

## Nodes

1. `git-workflow`: 저장소와 작업 트리를 확인하고 최신 `main`에서 기능 전용 브랜치를 준비한다.
2. `requirement-analyze`: 기존 규칙 안에서 기능 요구사항, 구현 범위와 미결정 사항을 분석한다.
3. `docs-sync`: 구현에 앞서 확정된 도메인·아키텍처·테스트 규칙 변경을 관련 문서에 반영한다.
4. `tdd-implementation`: 실패 테스트를 먼저 확인하고 기능과 필요한 Persist 구현을 최소 범위로 추가한다.
5. `flyway`: JPA Entity나 스키마가 변경되면 마이그레이션을 추가하고 실제 스키마와의 일치를 검증한다.
6. `code-review`: 도메인 규칙, 계층 경계, 회귀 위험과 테스트 충분성을 검토한다.
7. `git-workflow`: 전체 diff를 원자적인 단위로 commit하고, 승인된 경우 원격 작업 브랜치에 push한다.

## Git Gate

- 파일을 수정하기 전에 작업 전용 브랜치 checkout이 완료되어야 한다.
- 시작 단계의 브랜치 준비와 종료 단계의 commit·push를 하나의 후행 Git 작업으로 합치지 않는다.
- 사용자가 브랜치명, commit 또는 push를 이미 요청했다면 같은 작업에 대한 동의를 다시 요청하지 않는다.
- push 보고는 마지막 커밋이 아니라 기준 브랜치부터 현재 HEAD까지의 전체 커밋과 diff를 기준으로 작성한다.
- 브랜치 준비가 실패하거나 현재 변경을 안전하게 보존할 수 없으면 구현을 시작하지 않는다.

## Conditional Nodes

- 요구사항 분석에서 기존 도메인 규칙 자체를 변경해야 한다면 `domain-change` Workflow로 전환한다.
- Repository를 추가하거나 변경하면 실제 데이터베이스를 사용하는 저장·조회 통합 테스트를 작성한다.
- JPA Entity가 변경되면 `flyway`를 반드시 실행한다.
- 구현 결과로 기준 문서와 불일치가 발견되면 `docs-sync`를 다시 실행한 뒤 재검토한다.
- 기능 범위를 넘어서는 구조 개선은 `refactoring` Workflow로 분리한다.
- `code-review`에서 완료를 막는 발견 사항이 나오면 해당 구현·문서 노드로 되돌아가 수정하고 다시 검토한다.
- 모든 발견 사항이 해결되기 전에는 마지막 `git-workflow`를 실행하지 않는다.

## Verification Gate

- [ ] 구현 전에 최신 `main` 기준의 작업 브랜치가 준비되었는가?
- [ ] 미결정 요구사항 없이 실패 테스트와 최소 구현 순서를 지켰는가?
- [ ] Persist 변경을 실제 데이터베이스로 검증했는가?
- [ ] Entity 변경에 필요한 Flyway 마이그레이션을 확인했는가?
- [ ] `code-review`가 완료되었는가?
- [ ] commit·push 대상이 전체 diff와 일치하는가?

## Exit Criteria

- 기능, 관련 테스트와 조건부 문서·마이그레이션 변경이 일관되게 완료되었다.
- 모든 Verification Gate를 충족하고 `code-review`의 완료 차단 발견 사항을 해결했다.
- 변경사항이 작업 브랜치에 원자적으로 commit되었다.
- push가 승인된 경우 로컬과 원격 작업 브랜치의 HEAD가 일치한다.
