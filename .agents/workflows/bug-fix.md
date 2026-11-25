---
name: bug-fix
description: 오류나 실패 테스트의 원인을 분석하고 회귀 테스트와 함께 수정할 때 사용한다.
---

## Flow

```text
git-workflow
        ↓
code-analyze
        ↓
tdd-implementation
        ↓
code-review
```

## Nodes

1. `git-workflow`: 최신 `main`을 동기화하고 작업 브랜치를 준비한다.
2. `code-analyze`: 재현 조건, 실행 흐름, 원인과 영향 범위를 분석한다.
3. `tdd-implementation`: 실패를 재현하는 테스트를 작성하고 최소 수정으로 해결한다.
4. `code-review`: 수정 범위, 회귀 위험과 테스트 충분성을 검토한다.

## Conditional Nodes

- 원인이 도메인 규칙의 누락·모호함·충돌이면 `requirement-analyze`를 `tdd-implementation` 전에 실행한다.
- 규칙이나 기술 문서가 변경되면 `docs-sync`를 실행한다.
- 구현 전 `git-workflow`를 항상 실행한다.
- 사용자가 commit 또는 push를 요청하면 구현과 검토 후 `git-workflow`를 다시 실행한다.

## Exit Criteria

- 원인과 수정 결과가 테스트로 검증되었다.
- `code-review`가 완료되었다.
- 조건부 노드가 필요하면 해당 노드까지 완료되었다.
