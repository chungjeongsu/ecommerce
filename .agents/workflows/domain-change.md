---
name: domain-change
description: 전용 Git 작업 브랜치에서 도메인 규칙이나 불변식을 변경하고 관련 문서와 구현을 함께 갱신할 때 사용한다.
---

## Flow

```text
git-workflow (작업 브랜치 준비)
        ↓
requirement-analyze
        ↓
docs-sync
        ↓
tdd-implementation
        ↓
[JPA Entity 변경 시] flyway
        ↓
docs-sync (최종 일치 확인)
        ↓
code-review
        ↓
git-workflow (commit · push)
```

## Nodes

1. `git-workflow`: 저장소와 작업 트리를 확인하고 최신 `main`에서 도메인 변경 전용 브랜치를 준비한다.
2. `requirement-analyze`: 변경할 규칙, 영향 도메인, 충돌과 미결정 사항을 분석한다.
3. `docs-sync`: 사용자가 확정한 규칙을 구현 전에 기준 문서에 반영한다.
4. `tdd-implementation`: 변경된 규칙을 실패 테스트와 최소 구현으로 반영한다.
5. `flyway`: JPA Entity나 스키마가 변경되면 마이그레이션을 추가하고 실제 스키마와의 일치를 검증한다.
6. `docs-sync`: 검증된 최종 구현과 기준 문서가 일치하는지 확인하고 필요한 범위만 보정한다.
7. `code-review`: 규칙 변경의 영향, Aggregate 경계, 문서 일치, 회귀 위험과 테스트 충분성을 검토한다.
8. `git-workflow`: 전체 diff를 원자적인 단위로 commit하고, 승인된 경우 원격 작업 브랜치에 push한다.

## Git Gate

- 문서나 코드를 수정하기 전에 작업 전용 브랜치 checkout이 완료되어야 한다.
- 시작 단계의 브랜치 준비와 종료 단계의 commit·push를 하나의 후행 Git 작업으로 합치지 않는다.
- 사용자가 브랜치명, commit 또는 push를 이미 요청했다면 같은 작업에 대한 동의를 다시 요청하지 않는다.
- push 보고는 마지막 커밋이 아니라 기준 브랜치부터 현재 HEAD까지의 전체 커밋과 diff를 기준으로 작성한다.
- 브랜치 준비가 실패하거나 현재 변경을 안전하게 보존할 수 없으면 문서·구현 변경을 시작하지 않는다.

## Conditional Nodes

- 미확정 규칙이나 문서 충돌이 남아 있으면 `docs-sync`와 구현을 진행하지 않는다.
- Repository를 추가하거나 변경하면 실제 데이터베이스를 사용하는 저장·조회 통합 테스트를 작성한다.
- JPA Entity가 변경되면 `flyway`를 반드시 실행한다.
- 구현 결과가 확정 문서와 다르면 임의로 규칙을 바꾸지 않고 `requirement-analyze`로 되돌린다.
- `code-review`에서 완료를 막는 발견 사항이 나오면 해당 분석·문서·구현 노드로 되돌아가 수정하고 다시 검토한다.
- 모든 발견 사항이 해결되기 전에는 마지막 `git-workflow`를 실행하지 않는다.

## Verification Gate

- [ ] 변경 전에 최신 `main` 기준의 작업 브랜치가 준비되었는가?
- [ ] 규칙의 누락·모호함·충돌이 해소되었는가?
- [ ] 확정 규칙이 구현 전에 기준 문서에 반영되었는가?
- [ ] Persist·Flyway 변경을 필요한 실제 환경에서 검증했는가?
- [ ] 관련 테스트와 `code-review`가 완료되었는가?
- [ ] 최종 문서와 구현이 일치하는가?
- [ ] commit·push 대상이 전체 diff와 일치하는가?

## Exit Criteria

- 규칙 변경이 문서, 구현, 테스트와 필요한 마이그레이션에 일관되게 반영되었다.
- 모든 Verification Gate를 충족하고 `code-review`의 완료 차단 발견 사항을 해결했다.
- 변경사항이 작업 브랜치에 원자적으로 commit되었다.
- push가 승인된 경우 로컬과 원격 작업 브랜치의 HEAD가 일치한다.
