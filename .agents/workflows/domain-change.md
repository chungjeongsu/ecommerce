---
name: domain-change
description: 도메인 규칙이나 불변식을 변경하고 관련 문서와 구현을 함께 갱신할 때 사용한다.
---

## Flow

```text
requirement-analyze
        ↓
docs-sync
        ↓
tdd-implementation
        ↓
code-review
```

## Nodes

1. `requirement-analyze`: 변경할 규칙, 영향 도메인과 미결정 사항을 분석한다.
2. `docs-sync`: 확정된 규칙을 도메인·아키텍처·테스트 문서에 반영한다.
3. `tdd-implementation`: 변경된 규칙을 테스트와 코드에 반영한다.
4. `code-review`: 규칙 변경의 영향, 경계와 회귀 위험을 검토한다.

## Conditional Nodes

- 구현 결과로 문서와 실제 규칙의 불일치가 발견되면 `docs-sync`를 다시 실행한다.
- 사용자가 commit, push 또는 브랜치 작업을 요청하면 `git-workflow`를 실행한다.

## Exit Criteria

- 규칙 변경이 문서와 구현에 일관되게 반영되었다.
- 관련 테스트와 `code-review`가 완료되었다.
- 조건부 노드가 필요하면 해당 노드까지 완료되었다.
