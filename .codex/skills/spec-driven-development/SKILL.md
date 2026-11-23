---
name: spec-driven-development
description: Refine a request into explicit requirements, responsibilities, invariants, lifecycles, state transitions, and acceptance criteria before implementation.
---

# Spec Driven Development

## FrontMatter

- 실행 조건: 새로운 기능, 도메인 변경, 상태 전이 변경, 요구사항 검증
- 실행하지 않는 조건: 요구사항과 불변식이 이미 명확하고 단순한 기계적 수정인 경우

## Input Contract

- 사용자 요청
- 관련 도메인 문서
- 기존 코드와 테스트
- Context Engineering 결과

## Steps

1. 요구사항을 외부에서 관찰 가능한 행위로 정리한다.
2. 객체, Aggregate, 도메인 간 협력 관계를 식별한다.
3. 객체 불변식, 도메인 불변식, 외부 불변식을 구분한다.
4. 각 객체와 도메인의 생명주기와 상태 전이를 정리한다.
5. 정상 흐름, 예외 흐름, 경계 조건을 정의한다.
6. 기존 문서·코드·테스트와 충돌하는 내용을 식별한다.
7. 미확정 규칙이 있으면 영향 범위가 큰 항목부터 하나씩 질문한다.

## Output Contract

- 요구사항 목록
- 객체와 Aggregate 책임
- 불변식 목록
- 생명주기와 상태 전이
- 협력 흐름
- 수용 조건
- 미확정 질문

## Verification Gate

- [ ] 외부·도메인·객체 불변식을 구분했는가?
- [ ] 상태 전이와 전이 조건을 정의했는가?
- [ ] 정상·예외·경계 흐름을 포함했는가?
- [ ] 미확정 규칙을 임의로 확정하지 않았는가?

## Failure Policy

- 문서와 요청이 충돌하면 구현하지 않고 사용자에게 질문한다.
- 생명주기나 상태 전이가 누락되었다고 의심되면 해당 항목을 질문한다.
- 요구사항이 테스트 가능한 행위로 표현되지 않으면 먼저 정제한다.

## Exit Criteria

- 구현과 테스트에 사용할 명세가 작성되었다.
- 미확정 비즈니스 규칙이 없거나 사용자에게 전달되었다.
- TDD 구현에 필요한 수용 조건이 명확하다.

## Anti-Rationalization

- “일반적으로 그렇게 한다”는 이유로 도메인 규칙을 추가하지 않는다.
- 상태가 없는 객체를 상태가 있다고 가정하지 않는다.
- 여러 도메인의 책임을 하나의 객체에 임의로 합치지 않는다.
- 질문해야 할 규칙을 구현 편의상 추정하지 않는다.
