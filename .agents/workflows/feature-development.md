---
name: feature-development
description: 전용 Git 작업 브랜치에서 기존 도메인 규칙을 유지한 새 기능을 분석하고 TDD로 구현·검토할 때 사용한다.
---

## Flow

```text
git-checkout
        ↓
domain-requirement-analyze
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

1. `git-checkout`: 저장소와 작업 트리를 확인하고 동기화된 `main`에서 사용자가 확인한 기능 브랜치를 준비한다.
2. `domain-requirement-analyze`: 사용자 요구사항을 도메인 규칙과 비교해 적용 규칙과 미결정 사항을 분석한다.
3. `docs-sync`: 질문·답변으로 변경이 확정된 규칙만 관련 기준 문서에 반영한다.
4. `code-requirement-analyze`: 확정된 요구사항을 코드 변경 범위, 테스트 범위와 인프라 영향으로 변환한다.
5. `tdd-implementation`: 실패 테스트, 최소 구현과 리팩터링 순서로 기능을 구현하고 검증한다.
6. `flyway`: Entity나 스키마 변경이 있으면 마이그레이션을 작성하고 실제 스키마와의 일치를 검증한다.
7. `code-review`: 도메인 규칙, 아키텍처 경계, 회귀 위험과 테스트 충분성을 판정한다.
8. `git-commit`: 검증된 전체 변경을 목적별 원자적 커밋으로 기록한다.
9. `git-push`: push 승인이 있으면 전체 변경을 확인하고 원격 작업 브랜치에 반영한다.

## Git Gate

- 파일을 수정하기 전에 `git-checkout`이 완료되어야 한다.
- 작업 트리에 변경이 있으면 사용자에게 처리 방법을 확인한다.
- 브랜치명 후보는 자연어 요청에서 추출하고 사용자 확인 후 사용한다.
- 상세 브랜치명은 영문 소문자 kebab-case로 구성한다.
- 작업 브랜치가 이미 존재하면 새 브랜치명을 질문한다.
- `main` 동기화나 브랜치 생성이 실패하면 분석과 구현을 시작하지 않는다.
- `git-commit`과 `git-push`는 분리하여 실행한다.
- 사용자가 commit 또는 push를 이미 요청했다면 같은 작업의 승인을 다시 요청하지 않는다.
- push 보고는 마지막 커밋이 아니라 기준 브랜치부터 현재 HEAD까지의 전체 커밋과 diff를 기준으로 작성한다.

## Conditional Nodes

- 요구사항이 모호하거나 미결정 상태면 질문하고 해당 결정에 의존하는 작업을 중단한다.
- 사용자 답변 없이 도메인·아키텍처·테스트 규칙 문서를 변경하지 않는다.
- 기존 도메인 규칙 자체를 변경해야 한다면 `domain-change` Workflow로 전환한다.
- 확정되지 않은 도메인 규칙이 있으면 `code-requirement-analyze`와 구현을 진행하지 않는다.
- 코드 변경 범위나 공개 계약 영향이 불명확하면 구현을 진행하지 않는다.
- Repository를 추가하거나 변경하면 실제 데이터베이스를 사용하는 저장·조회 통합 테스트를 작성한다.
- JPA Entity가 변경되면 `flyway`를 반드시 실행한다.
- Java 접근 제어나 도메인 행위만 변경되고 스키마가 동일하면 `flyway`를 실행하지 않는다.
- Entity와 스키마가 불일치하면 구현 또는 마이그레이션 책임 Skill에서 수정하고 다시 검증한다.
- 기능 범위를 넘어서는 구조 개선은 `refactoring` Workflow로 분리한다.
- 문서와 구현이 다르면 문서를 자동 수정하지 않고 사용자 결정을 요청한다.
- `code-review`는 코드를 수정하지 않고 발견 사항만 반환한다.
- 리뷰 발견 사항은 해당 책임 Skill에서 수정하고 다시 검토한다.
- 완료 차단 발견 사항이 남아 있으면 `git-commit`을 실행하지 않는다.
- push 승인이 없으면 `git-push`를 실행하지 않는다.

## Verification Gate

- [ ] 구현 전에 최신 `main` 기준의 작업 브랜치가 준비되었는가?
- [ ] 도메인 요구사항의 모호·미결정·충돌이 해소되었는가?
- [ ] 문서 변경은 사용자 질문·답변으로 확정된 내용만 포함하는가?
- [ ] 요구사항과 코드·테스트·인프라 영향의 대응 관계를 확인했는가?
- [ ] 실패 테스트와 최소 구현 순서를 지켰는가?
- [ ] Repository 변경을 실제 데이터베이스로 검증했는가?
- [ ] Entity 변경에 필요한 Flyway 마이그레이션을 확인했는가?
- [ ] Entity와 실제 스키마가 일치하는가?
- [ ] `code-review`의 완료 차단 발견 사항이 모두 해결되었는가?
- [ ] commit 대상이 전체 diff와 일치하는가?
- [ ] push한 경우 로컬과 원격 HEAD가 일치하는가?

## Exit Criteria

- 완료: 기능, 테스트와 조건부 문서·마이그레이션 변경이 일관되게 검증되고 원자적으로 commit되었다.
- 대기: 사용자 결정, commit 또는 push 승인이 필요하다.
- 실패: 필수 Skill이나 Verification Gate를 완료할 수 없다.
- 취소: 사용자가 기능 개발을 취소했다.
