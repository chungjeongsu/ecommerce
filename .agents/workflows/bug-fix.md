---
name: bug-fix
description: 전용 Git 작업 브랜치에서 오류나 실패 테스트의 원인을 분석하고 회귀 테스트와 함께 수정할 때 사용한다.
---

## Flow

```text
git-workflow (작업 브랜치 준비)
        ↓
code-analyze
        ↓
[도메인 판단 필요 시] requirement-analyze
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

1. `git-workflow`: 저장소와 작업 트리를 확인하고 최신 `main`에서 수정 전용 브랜치를 준비한다.
2. `code-analyze`: 재현 조건, 실행 흐름, 확정 원인과 영향 범위를 분석한다.
3. `requirement-analyze`: 원인이 도메인 규칙의 누락·모호함·충돌과 관련된 경우 필요한 결정을 확정한다.
4. `docs-sync`: 수정으로 달라지는 확정 규칙을 관련 기준 문서에 먼저 반영한다.
5. `tdd-implementation`: 결함을 재현하는 실패 테스트를 확인하고 최소 수정으로 해결한다.
6. `flyway`: JPA Entity나 스키마 수정이 필요하면 마이그레이션과 실제 스키마 일치를 검증한다.
7. `code-review`: 원인과 수정의 대응 관계, 회귀 위험, 테스트 충분성과 변경 범위를 검토한다.
8. `git-workflow`: 전체 diff를 원자적인 단위로 commit하고, 승인된 경우 원격 작업 브랜치에 push한다.

## Git Gate

- 파일을 수정하기 전에 작업 전용 브랜치 checkout이 완료되어야 한다.
- 시작 단계의 브랜치 준비와 종료 단계의 commit·push를 하나의 후행 Git 작업으로 합치지 않는다.
- 사용자가 브랜치명, commit 또는 push를 이미 요청했다면 같은 작업에 대한 동의를 다시 요청하지 않는다.
- push 보고는 마지막 커밋이 아니라 기준 브랜치부터 현재 HEAD까지의 전체 커밋과 diff를 기준으로 작성한다.
- 브랜치 준비가 실패하거나 현재 변경을 안전하게 보존할 수 없으면 수정 구현을 시작하지 않는다.

## Conditional Nodes

- 재현이나 원인 분석이 완료되지 않으면 구현으로 진행하지 않는다.
- 도메인 결정이 필요하면 `requirement-analyze`와 `docs-sync`를 구현 전에 실행한다.
- Repository를 추가하거나 변경하면 실제 데이터베이스를 사용하는 회귀 통합 테스트를 작성한다.
- JPA Entity가 변경되면 `flyway`를 반드시 실행한다.
- 수정 범위가 새 기능 추가로 확장되면 `feature-development` Workflow로 전환한다.
- `code-review`에서 완료를 막는 발견 사항이 나오면 해당 분석·구현·문서 노드로 되돌아가 수정하고 다시 검토한다.
- 모든 발견 사항이 해결되기 전에는 마지막 `git-workflow`를 실행하지 않는다.

## Verification Gate

- [ ] 구현 전에 최신 `main` 기준의 작업 브랜치가 준비되었는가?
- [ ] 결함을 재현하는 테스트가 수정 전에 실패했는가?
- [ ] 확인된 원인에 대응하는 최소 수정만 포함했는가?
- [ ] Persist·Flyway 변경을 필요한 실제 환경에서 검증했는가?
- [ ] 관련 회귀 테스트와 `code-review`가 완료되었는가?
- [ ] commit·push 대상이 전체 diff와 일치하는가?

## Exit Criteria

- 원인과 수정 결과가 회귀 테스트로 검증되었다.
- 모든 Verification Gate를 충족하고 `code-review`의 완료 차단 발견 사항을 해결했다.
- 변경사항이 작업 브랜치에 원자적으로 commit되었다.
- push가 승인된 경우 로컬과 원격 작업 브랜치의 HEAD가 일치한다.
