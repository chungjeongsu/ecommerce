---
name: refactoring
description: 전용 Git 작업 브랜치에서 외부 동작과 도메인 규칙을 유지하며 코드 구조와 책임을 개선할 때 사용한다.
---

## Flow

```text
git-workflow (작업 브랜치 준비)
        ↓
code-analyze
        ↓
tdd-implementation
        ↓
[아키텍처 문서 변경 시] docs-sync
        ↓
code-review
        ↓
git-workflow (commit · push)
```

## Nodes

1. `git-workflow`: 저장소와 작업 트리를 확인하고 최신 `main`에서 리팩터링 전용 브랜치를 준비한다.
2. `code-analyze`: 현재 구조, 책임, 결합도, 보존할 동작과 영향 범위를 분석한다.
3. `tdd-implementation`: 기존 동작을 보호하는 테스트를 먼저 확인하고 구조를 점진적으로 개선한다.
4. `docs-sync`: 책임이나 아키텍처 규칙이 변경되면 검증된 최종 구조를 관련 문서에 반영한다.
5. `code-review`: 동작 보존, 도메인 불변식, 아키텍처 경계와 변경 범위를 검토한다.
6. `git-workflow`: 전체 diff를 원자적인 단위로 commit하고, 승인된 경우 원격 작업 브랜치에 push한다.

## Git Gate

- 파일을 수정하기 전에 작업 전용 브랜치 checkout이 완료되어야 한다.
- 시작 단계의 브랜치 준비와 종료 단계의 commit·push를 하나의 후행 Git 작업으로 합치지 않는다.
- 사용자가 브랜치명, commit 또는 push를 이미 요청했다면 같은 작업에 대한 동의를 다시 요청하지 않는다.
- push 보고는 마지막 커밋이 아니라 기준 브랜치부터 현재 HEAD까지의 전체 커밋과 diff를 기준으로 작성한다.
- 브랜치 준비가 실패하거나 현재 변경을 안전하게 보존할 수 없으면 리팩터링을 시작하지 않는다.

## Conditional Nodes

- 보호 테스트가 부족하면 현재 동작을 검증하는 테스트를 먼저 추가한다.
- 작업 중 외부 동작이나 도메인 규칙 변경이 필요해지면 중단하고 `feature-development` 또는 `domain-change` Workflow로 전환한다.
- JPA Entity나 스키마 변경이 필요해지면 단순 리팩터링 범위를 벗어난 것으로 판단하고 적합한 Workflow로 전환한다.
- 검증된 구조가 아키텍처 문서와 달라지면 `docs-sync` 후 다시 검토한다.
- `code-review`에서 완료를 막는 발견 사항이 나오면 분석 또는 구현 노드로 되돌아가 수정하고 다시 검토한다.
- 모든 발견 사항이 해결되기 전에는 마지막 `git-workflow`를 실행하지 않는다.

## Verification Gate

- [ ] 구현 전에 최신 `main` 기준의 작업 브랜치가 준비되었는가?
- [ ] 변경 전 보존할 동작과 테스트 상태를 확인했는가?
- [ ] 외부 동작과 도메인 불변식이 유지되었는가?
- [ ] 범위를 벗어난 기능·규칙·스키마 변경이 포함되지 않았는가?
- [ ] 관련 테스트와 `code-review`가 완료되었는가?
- [ ] commit·push 대상이 전체 diff와 일치하는가?

## Exit Criteria

- 외부 동작과 도메인 불변식이 유지된 상태로 구조가 개선되었다.
- 모든 Verification Gate를 충족하고 `code-review`의 완료 차단 발견 사항을 해결했다.
- 변경사항이 작업 브랜치에 원자적으로 commit되었다.
- push가 승인된 경우 로컬과 원격 작업 브랜치의 HEAD가 일치한다.
