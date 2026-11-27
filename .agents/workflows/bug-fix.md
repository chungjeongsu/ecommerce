---
name: bug-fix
description: 전용 Git 작업 브랜치에서 오류나 실패 테스트의 원인을 분석하고 회귀 테스트와 함께 수정할 때 사용한다.
---

## Flow

```text
git-checkout
        ↓
code-analyze
        ↓
[도메인 판단 필요 시] domain-requirement-analyze
        ↓
[질문·답변으로 규칙 변경 확정 시] docs-sync
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

1. `git-checkout`: 저장소와 작업 트리를 확인하고 동기화된 `main`에서 사용자가 확인한 수정 브랜치를 준비한다.
2. `code-analyze`: 재현 조건, 실행 흐름, 확정 원인과 영향 범위를 분석한다.
3. `domain-requirement-analyze`: 원인이 도메인 규칙의 누락·모호함·충돌과 관련된 경우 적용 규칙과 미결정 사항을 분석한다.
4. `docs-sync`: 질문·답변으로 변경이 확정된 규칙만 관련 기준 문서에 반영한다.
5. `code-requirement-analyze`: 확정된 원인과 기대 동작을 수정 범위, 회귀 테스트와 인프라 영향으로 변환한다.
6. `tdd-implementation`: 결함을 재현하는 실패 테스트를 확인하고 최소 수정으로 해결한다.
7. `flyway`: Entity나 스키마 수정이 있으면 마이그레이션과 실제 스키마 일치를 검증한다.
8. `code-review`: 원인과 수정의 대응 관계, 회귀 위험, 테스트 충분성과 변경 범위를 판정한다.
9. `git-commit`: 검증된 전체 변경을 목적별 원자적 커밋으로 기록한다.
10. `git-push`: push 승인이 있으면 전체 변경을 확인하고 원격 작업 브랜치에 반영한다.

## Git Gate

- 파일을 수정하기 전에 `git-checkout`이 완료되어야 한다.
- `main` 동기화나 수정 브랜치 생성이 실패하면 분석과 구현을 시작하지 않는다.
- `git-commit`과 `git-push`는 분리하여 실행한다.
- 사용자가 브랜치명, commit 또는 push를 이미 요청했다면 같은 작업에 대한 동의를 다시 요청하지 않는다.
- push 보고는 마지막 커밋이 아니라 기준 브랜치부터 현재 HEAD까지의 전체 커밋과 diff를 기준으로 작성한다.

## Conditional Nodes

- 재현 조건, 실행 흐름과 원인 분석이 완료되지 않으면 구현으로 진행하지 않는다.
- 자동 재현이 불가능하면 대체 근거와 미검증 범위를 제시하고 사용자 확인 전까지 구현하지 않는다.
- 도메인 판단이 필요하면 `domain-requirement-analyze`를 실행한다.
- 사용자 답변 없이 규칙 문서를 변경하지 않는다.
- 질문·답변으로 규칙 변경이 확정된 경우에만 `docs-sync`를 실행한다.
- 원인, 기대 동작과 수정 범위가 확정되지 않으면 `tdd-implementation`을 실행하지 않는다.
- Repository를 추가하거나 변경하면 실제 데이터베이스를 사용하는 회귀 통합 테스트를 작성한다.
- JPA Entity가 변경되면 `flyway`를 반드시 실행한다.
- 수정 범위가 새 기능 추가로 확장되면 `feature-development` Workflow로 전환한다.
- `code-review`는 코드를 수정하지 않고 발견 사항만 반환한다.
- 리뷰 발견 사항은 해당 책임 Skill에서 수정하고 다시 검토한다.
- 완료 차단 발견 사항이 남아 있으면 `git-commit`을 실행하지 않는다.
- push 승인이 없으면 `git-push`를 실행하지 않는다.

## Verification Gate

- [ ] 구현 전에 최신 `main` 기준의 작업 브랜치가 준비되었는가?
- [ ] 재현 조건, 실행 흐름과 원인이 근거로 확인되었는가?
- [ ] 결함을 재현하는 테스트가 수정 전에 실패했는가?
- [ ] 확인된 원인에 대응하는 최소 수정만 포함했는가?
- [ ] 요구사항과 코드·테스트·인프라 영향의 대응 관계를 확인했는가?
- [ ] Repository와 Flyway 변경을 필요한 실제 환경에서 검증했는가?
- [ ] 관련 회귀 테스트가 통과했는가?
- [ ] `code-review`의 완료 차단 발견 사항이 모두 해결되었는가?
- [ ] commit 대상이 전체 diff와 일치하는가?
- [ ] push한 경우 로컬과 원격 HEAD가 일치하는가?

## Exit Criteria

- 완료: 원인과 수정 결과가 회귀 테스트로 검증되고 변경이 원자적으로 commit되었다.
- 대기: 재현 근거, 사용자 결정, commit 또는 push 승인이 필요하다.
- 실패: 필수 Skill이나 Verification Gate를 완료할 수 없다.
- 취소: 사용자가 버그 수정을 취소했다.
