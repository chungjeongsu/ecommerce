---
name: refactoring
description: 전용 Git 작업 브랜치에서 외부 동작과 도메인 규칙을 유지하며 코드 구조와 책임을 개선할 때 사용한다.
---

## Flow

```text
git-checkout
        ↓
code-analyze
        ↓
code-requirement-analyze
        ↓
[질문·답변으로 아키텍처 규칙 변경 확정 시] docs-sync
        ↓
refactoring-implementation
        ↓
code-review
        ↓
git-commit
        ↓
[승인 시] git-push
```

## Nodes

1. `git-checkout`: 저장소와 작업 트리를 확인하고 동기화된 `main`에서 사용자가 확인한 리팩터링 브랜치를 준비한다.
2. `code-analyze`: 현재 구조, 책임, 결합도, 보존할 동작과 영향 범위를 분석한다.
3. `code-requirement-analyze`: 목표 구조, 변경 범위, 보호 테스트와 아키텍처 영향을 분석한다.
4. `docs-sync`: 질문·답변으로 변경이 확정된 아키텍처 규칙만 구현 전에 문서에 반영한다.
5. `refactoring-implementation`: 보호 테스트 아래 외부 동작과 규칙을 유지하며 구조를 작은 단위로 개선한다.
6. `code-review`: 동작 보존, 도메인 불변식, 아키텍처 경계와 변경 범위를 판정한다.
7. `git-commit`: 검증된 전체 변경을 목적별 원자적 커밋으로 기록한다.
8. `git-push`: push 승인이 있으면 전체 변경을 확인하고 원격 작업 브랜치에 반영한다.

## Git Gate

- 파일을 수정하기 전에 `git-checkout`이 완료되어야 한다.
- `main` 동기화나 작업 브랜치 생성이 실패하면 리팩터링을 시작하지 않는다.
- `git-commit`과 `git-push`는 분리하여 실행한다.
- 사용자가 브랜치명, commit 또는 push를 이미 요청했다면 같은 작업에 대한 동의를 다시 요청하지 않는다.
- push 보고는 마지막 커밋이 아니라 기준 브랜치부터 현재 HEAD까지의 전체 커밋과 diff를 기준으로 작성한다.

## Conditional Nodes

- 보호 테스트가 부족하면 현재 동작을 검증하는 테스트를 먼저 추가한다.
- 외부 동작 변경이 필요하면 중단하고 `feature-development` Workflow로 전환한다.
- 도메인 규칙 변경이 필요하면 중단하고 `domain-change` Workflow로 전환한다.
- Entity나 스키마 변경이 필요하면 리팩터링을 중단한다.
- 사용자 답변 없이 아키텍처 규칙 문서를 변경하지 않는다.
- 아키텍처 규칙 변경이 확정된 경우에만 `docs-sync`를 실행한다.
- 구현 결과를 근거로 아키텍처 문서를 자동 변경하지 않는다.
- `code-review`는 코드를 수정하지 않고 발견 사항만 반환한다.
- 리뷰 발견 사항은 해당 책임 Skill에서 수정하고 다시 검토한다.
- 완료 차단 발견 사항이 남아 있으면 `git-commit`을 실행하지 않는다.
- push 승인이 없으면 `git-push`를 실행하지 않는다.

## Verification Gate

- [ ] 구현 전에 최신 `main` 기준의 작업 브랜치가 준비되었는가?
- [ ] 변경 전 보존할 동작과 테스트 상태를 확인했는가?
- [ ] 목표 구조와 변경 범위가 확정되었는가?
- [ ] 보호 테스트가 변경 전후 모두 통과했는가?
- [ ] 외부 동작과 도메인 불변식이 유지되었는가?
- [ ] 범위를 벗어난 기능·규칙·스키마 변경이 포함되지 않았는가?
- [ ] 아키텍처 문서 변경은 사용자 질문·답변으로 확정된 내용만 포함하는가?
- [ ] `code-review`의 완료 차단 발견 사항이 모두 해결되었는가?
- [ ] commit 대상이 전체 diff와 일치하는가?
- [ ] push한 경우 로컬과 원격 HEAD가 일치하는가?

## Exit Criteria

- 완료: 외부 동작과 도메인 규칙을 보존한 구조 개선이 검증되고 원자적으로 commit되었다.
- 대기: 구조 결정, 아키텍처 규칙 변경, commit 또는 push 승인이 필요하다.
- 실패: 필수 Skill이나 Verification Gate를 완료할 수 없다.
- 취소: 사용자가 리팩터링을 취소했다.
