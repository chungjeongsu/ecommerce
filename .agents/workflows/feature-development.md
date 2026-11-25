---
name: feature-development
description: 기존 도메인 규칙을 유지한 새 기능을 분석하고 TDD로 구현·검토할 때 사용한다.
---

## Flow

```text
[특정 브랜치 요청 시]
git-workflow
        ↓
requirement-analyze
        ↓
tdd-implementation
        ↓
code-review
```

## Nodes

1. `requirement-analyze`: 기존 규칙 안에서 기능 요구사항과 적용 범위를 분석한다.
2. `tdd-implementation`: 확정된 규칙을 실패 테스트와 최소 구현으로 반영한다.
3. `code-review`: 구현 결과와 테스트를 규칙·경계·회귀 위험 관점에서 검토한다.

## Conditional Nodes

- 사용자가 특정 브랜치에서 구현하라고 요청하면 `git-workflow`를 구현 전에 실행한다.
- 도메인·아키텍처·테스트 문서의 규칙을 변경해야 하면 `docs-sync`를 실행한다.
- 사용자가 별도 리팩토링을 요청하거나 구현 후 구조 개선이 기능 범위를 넘어가면 `refactoring`을 별도 Workflow로 실행한다.
- 사용자가 commit 또는 push를 요청하면 구현과 검토 후 `git-workflow`를 실행한다.
- 동일 요청에서 브랜치 전환과 commit·push가 모두 필요하면 `git-workflow`가 두 작업을 함께 처리한다.

## Exit Criteria

- 필수 노드의 Output이 다음 노드로 전달되었다.
- `code-review`가 완료되었다.
- 조건부 노드가 필요하면 해당 노드까지 완료되었다.
