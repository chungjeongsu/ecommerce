---
name: code-review-and-quality
description: Review completed changes for requirement coverage, domain integrity, architecture boundaries, test quality, and material regressions.
---

# Code Review and Quality

## FrontMatter

- 실행 조건: 구현·디버깅·리팩터링·문서 반영 후 결과 검토가 필요할 때
- 실행하지 않는 조건: 코드나 문서 변경이 없는 단순 질문

## Input Contract

- 사용자 요구사항
- 변경 파일과 diff
- 관련 도메인·아키텍처·테스트 문서
- 실행한 테스트 결과
- 이전 분석 결과

## Steps

1. 변경 범위와 의도된 동작을 확인한다.
2. 요구사항과 수용 조건이 모두 반영되었는지 확인한다.
3. 불변식, Aggregate 경계, 객체 책임을 검토한다.
4. 상태 전이와 도메인 간 의존성을 검토한다.
5. 계층·패키지·의존성 규칙을 검토한다.
6. 테스트 누락, 취약한 테스트, 기존 기능 영향을 확인한다.
7. 관련 테스트와 정적 검증 결과를 확인한다.
8. 문제를 심각도와 수정 필요성에 따라 보고한다.

## Output Contract

- 검토 결과
- 발견된 문제와 위치
- 심각도
- 수정 권장 사항
- 통과한 검증
- 남은 위험

## Verification Gate

- [ ] 변경 diff 전체를 확인했는가?
- [ ] 요구사항과 불변식을 대조했는가?
- [ ] 테스트 결과를 확인했는가?
- [ ] 문제를 근거와 함께 보고했는가?
- [ ] 발견하지 못한 영역을 과도하게 보증하지 않았는가?

## Failure Policy

- 중대한 결함이 있으면 완료로 보고하지 않고 수정을 요청한다.
- 근거가 부족한 문제는 사실과 추정을 구분해 보고한다.
- 테스트가 실행되지 않았다면 검토 완료와 테스트 통과를 혼동하지 않는다.

## Exit Criteria

- 변경 범위와 검토 결과가 명확하다.
- 중대한 미해결 문제가 없거나 사용자에게 보고되었다.
- 최종 검증 상태를 판단할 수 있다.

## Anti-Rationalization

- 코드가 동작한다는 이유로 설계 규칙 위반을 무시하지 않는다.
- 테스트가 통과했다는 이유로 요구사항 누락을 무시하지 않는다.
- 작은 변경이라는 이유로 diff와 영향 범위 확인을 생략하지 않는다.
