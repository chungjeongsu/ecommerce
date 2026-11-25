---
name: refactoring
description: 외부 동작과 도메인 규칙을 유지하면서 코드 구조와 책임을 개선할 때 사용한다.
---

## Flow

```text
code-analyze
        ↓
tdd-implementation
        ↓
code-review
```

## Nodes

1. `code-analyze`: 현재 구조, 책임, 결합도와 리팩터링 영향 범위를 분석한다.
2. `tdd-implementation`: 기존 동작을 보호하는 테스트를 확인하고 구조를 개선한다.
3. `code-review`: 동작 보존, 아키텍처 경계와 변경 범위를 검토한다.

## Conditional Nodes

- 아키텍처 규칙이나 문서가 변경되면 `docs-sync`를 실행한다.
- 사용자가 commit, push 또는 브랜치 작업을 요청하면 `git-workflow`를 실행한다.

## Exit Criteria

- 외부 동작과 도메인 불변식이 유지되었다.
- `code-review`가 완료되었다.
- 조건부 노드가 필요하면 해당 노드까지 완료되었다.
