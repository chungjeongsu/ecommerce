# Skill 설계 원칙

Skill은 특정 작업을 수행하는 실행 단위다.

## 설계 원칙

- 조건부 지침을 줄이고, 한 문장 당 단일 책임을 갖도록 작성한다.

## 구성 요소

- `Frontmatter`는 본문을 읽기 전에 Skill의 이름과 선택 기준을 제공한다.
- `Frontmatter`의 `description`은 Skill의 책임과 적용 상황을 구체적으로 설명한다.
- `Frontmatter`는 yml 형식으로 작성한다.
- `Boundary`는 Skill이 판단할 수 있는 범위를 정의한다.
- `Boundary`는 Skill이 실행할 수 있는 도구와 변경 범위를 정의한다.
- `Boundary`는 사용자 확인이 필요한 작업과 수행하면 안 되는 작업을 구분한다.
- `Input Contract`는 Skill 실행에 필요한 요청, 범위, 권한과 이전 결과를 정의한다.
- `Input Contract`는 확정된 정보와 미해결된 정보를 구분한다.
- `Input Contract`는 입력이 부족할 때 질문하거나 대기할 기준을 정의한다.
- `Responsibilities`는 Skill이 수행해야 하는 판단과 행동을 정의한다.
- `Responsibilities`는 Skill의 책임과 다른 Skill의 책임을 구분한다.
- `Responsibilities`는 작업을 완료하기 위해 필요한 도구 사용과 상태 확인을 정의한다.
- `Output Contract`는 Skill이 반환할 결과의 구조와 최소 정보를 정의한다.
- `Output Contract`는 변경 범위, 실행 결과, 검증 결과와 남은 위험을 전달한다.
- `Output Contract`는 다음 Skill이나 사용자에게 전달할 상태를 명확히 한다.
- `Verification Gate`는 완료 보고 전에 확인할 필수 조건을 정의한다.
- `Verification Gate`는 실제 상태, 실행 결과와 요청 범위의 일치 여부를 확인한다.
- `Verification Gate`를 통과하지 못하면 완료 상태로 보고하지 않는다.
- `Failure Policy`는 실패, 충돌, 권한 거부와 검증 불가 상태의 처리 기준을 정의한다.
- `Failure Policy`는 재시도 가능한 실패와 사용자 결정이 필요한 실패를 구분한다.
- `Failure Policy`는 실패 시 보존할 상태, 중단할 작업과 보고할 원인을 정의한다.
- `Exit Criteria`는 완료, 대기, 실패와 취소 상태를 구분하는 조건을 정의한다.
- `Exit Criteria`는 각 상태에서 다음에 수행할 작업이나 필요한 결정을 명시한다.

## 예시

```markdown
---
name: git-commit
description: 변경사항을 검증된 범위로 분리하고 한글 커밋 메시지로 기록한다.
---

## Boundary

### Always

- 현재 브랜치와 작업 트리 상태를 확인한다.
- staged diff가 요청 범위와 일치하는지 확인한다.

### Ask First

- 서로 다른 목적의 변경을 안전하게 분리할 수 없는 경우 확인한다.
- 요청 범위 밖의 변경을 함께 커밋해야 하는 경우 확인한다.

### Never

- 검증되지 않은 변경을 커밋하지 않는다.
- 사용자 승인 없이 관련 없는 파일을 커밋하지 않는다.

## Input Contract

- 커밋할 변경 범위
- 적용할 커밋 메시지 규칙

## Responsibilities

- 변경사항을 논리적 커밋 단위로 분리한다.
- 필요한 파일만 stage한다.
- staged diff와 커밋 메시지를 검증한다.

## Output Contract

- 커밋 ID와 메시지
- 커밋된 범위
- 남은 변경과 검증 결과

## Verification Gate

- [ ] staged diff가 요청 범위와 일치하는가?
- [ ] 커밋 메시지가 실제 변경을 설명하는가?

## Failure Policy

- 범위가 불명확하면 커밋하지 않고 사용자에게 확인한다.
- 검증이 실패하면 원인과 미커밋 상태를 보고한다.

## Exit Criteria

- 완료: 검증된 변경을 커밋하고 결과를 전달했다.
- 대기: 사용자 결정이 필요하다.
- 실패: 커밋 또는 필수 검증을 완료할 수 없다.
```
