---
name: planning-and-task-breakdown
description: Break complex work into bounded, ordered tasks with dependencies, checkpoints, and completion conditions.
---

# Planning and Task Breakdown

## FrontMatter

- 실행 조건: 여러 도메인·계층·문서·검증 단계가 포함된 복잡한 작업
- 실행하지 않는 조건: 범위와 검증 방법이 명확한 단순 작업

## Input Contract

- 사용자 목표
- Context Engineering 결과
- 요구사항과 제약
- 관련 코드·문서·테스트 범위

## Steps

1. 최종 목표와 완료 조건을 명확히 한다.
2. 작업을 독립적으로 검증 가능한 단위로 나눈다.
3. 선행 조건과 의존 관계를 식별한다.
4. 각 작업의 입력, 산출물, 검증 방법을 정한다.
5. 위험이 크거나 결정이 필요한 작업을 앞에 둔다.
6. 불필요한 병렬화와 과도한 분해를 제거한다.

## Output Contract

- 작업 목록
- 작업 순서
- 의존 관계
- 각 작업의 완료 조건
- 위험과 사용자 결정 사항

## Verification Gate

- [ ] 모든 작업이 최종 목표와 연결되는가?
- [ ] 선행 조건이 명확한가?
- [ ] 각 작업을 독립적으로 검증할 수 있는가?
- [ ] 범위 밖 작업이 포함되지 않았는가?

## Failure Policy

- 작업 간 의존성이 불명확하면 먼저 정리한다.
- 계획이 사용자 요청을 확장하면 범위를 줄이거나 확인한다.
- 실행 중 새 요구사항이 발견되면 현재 계획과 영향을 다시 평가한다.

## Exit Criteria

- 실행 가능한 작업 단위와 순서가 정해졌다.
- 각 작업의 완료 조건과 검증 방법이 있다.
- 계획에 포함되지 않은 결정 사항이 보고되었다.

## Anti-Rationalization

- 계획을 세웠다는 이유로 요구사항 분석과 검증을 생략하지 않는다.
- 작업을 잘게 나누는 것 자체를 진척으로 간주하지 않는다.
- 선행 조건이 충족되지 않았는데 다음 작업으로 넘어가지 않는다.
