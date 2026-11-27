---
name: domain-change
description: 전용 Git 작업 브랜치에서 도메인 규칙이나 불변식을 변경하고 관련 문서와 구현을 함께 갱신할 때 사용한다.
---

## Flow

```text
git-checkout
        ↓
domain-requirement-analyze
        ↓
사용자 질문·답변으로 규칙 확정
        ↓
docs-sync
        ↓
code-requirement-analyze
        ↓
tdd-implementation
        ↓
[JPA Entity 변경 시] flyway
        ↓
code-review
        ↓
git-commit
        ↓
[승인 시] git-push
```

## Nodes

1. `git-checkout`: 저장소와 작업 트리를 확인하고 동기화된 `main`에서 사용자가 확인한 도메인 변경 브랜치를 준비한다.
2. `domain-requirement-analyze`: 변경할 규칙, 영향 도메인, 충돌과 미결정 사항을 분석한다.
3. 사용자 질문·답변: 도메인 규칙과 생명주기 변경을 명시적으로 확정한다.
4. `docs-sync`: 사용자 답변으로 확정된 규칙만 구현 전에 기준 문서에 반영한다.
5. `code-requirement-analyze`: 확정된 규칙을 코드 변경 범위, 테스트 범위와 인프라 영향으로 변환한다.
6. `tdd-implementation`: 변경된 규칙을 실패 테스트와 최소 구현으로 반영한다.
7. `flyway`: Entity나 스키마가 변경되면 마이그레이션을 추가하고 실제 스키마와의 일치를 검증한다.
8. `code-review`: 규칙 변경의 영향, Aggregate 경계, 문서 일치, 회귀 위험과 테스트 충분성을 판정한다.
9. `git-commit`: 검증된 전체 변경을 목적별 원자적 커밋으로 기록한다.
10. `git-push`: push 승인이 있으면 전체 변경을 확인하고 원격 작업 브랜치에 반영한다.

## Git Gate

- 문서나 코드를 수정하기 전에 `git-checkout`이 완료되어야 한다.
- `main` 동기화나 작업 브랜치 생성이 실패하면 문서와 코드를 변경하지 않는다.
- `git-commit`과 `git-push`는 분리하여 실행한다.
- 사용자가 브랜치명, commit 또는 push를 이미 요청했다면 같은 작업에 대한 동의를 다시 요청하지 않는다.
- push 보고는 마지막 커밋이 아니라 기준 브랜치부터 현재 HEAD까지의 전체 커밋과 diff를 기준으로 작성한다.

## Conditional Nodes

- 미확정 규칙이나 문서 충돌이 남아 있으면 `docs-sync`와 구현을 진행하지 않는다.
- 사용자 질문·답변 없이 규칙 문서를 변경하지 않는다.
- `docs-sync`는 사용자 답변으로 확정된 규칙만 반영한다.
- 확정 규칙이 문서에 반영되기 전에는 `code-requirement-analyze`와 구현을 진행하지 않는다.
- Repository를 추가하거나 변경하면 실제 데이터베이스를 사용하는 저장·조회 통합 테스트를 작성한다.
- JPA Entity가 변경되면 `flyway`를 반드시 실행한다.
- 구현 결과가 확정 문서와 다르면 문서를 자동 수정하지 않는다.
- 불일치가 구현 오류면 구현 책임 Skill에서 수정한다.
- 새로운 도메인 결정이 필요하면 사용자 질문·답변으로 규칙을 다시 확정한다.
- 새 규칙이 확정된 경우에만 `docs-sync`를 다시 실행한다.
- `code-review`는 코드를 수정하지 않고 발견 사항만 반환한다.
- 리뷰 발견 사항은 해당 책임 Skill에서 수정하고 다시 검토한다.
- 완료 차단 발견 사항이 남아 있으면 `git-commit`을 실행하지 않는다.
- push 승인이 없으면 `git-push`를 실행하지 않는다.

## Verification Gate

- [ ] 변경 전에 최신 `main` 기준의 작업 브랜치가 준비되었는가?
- [ ] 규칙의 누락·모호함·충돌이 해소되었는가?
- [ ] 모든 규칙 변경이 사용자 질문·답변으로 확정되었는가?
- [ ] 확정 규칙만 구현 전에 기준 문서에 반영되었는가?
- [ ] 요구사항과 코드·테스트·인프라 영향의 대응 관계를 확인했는가?
- [ ] Repository와 Flyway 변경을 필요한 실제 환경에서 검증했는가?
- [ ] 관련 테스트가 통과했는가?
- [ ] `code-review`의 완료 차단 발견 사항이 모두 해결되었는가?
- [ ] 최종 문서와 구현이 일치하는가?
- [ ] commit 대상이 전체 diff와 일치하는가?
- [ ] push한 경우 로컬과 원격 HEAD가 일치하는가?

## Exit Criteria

- 완료: 확정 규칙이 문서, 구현, 테스트와 필요한 마이그레이션에 일관되게 반영되고 원자적으로 commit되었다.
- 대기: 사용자 결정, commit 또는 push 승인이 필요하다.
- 실패: 필수 Skill이나 Verification Gate를 완료할 수 없다.
- 취소: 사용자가 도메인 변경을 취소했다.
