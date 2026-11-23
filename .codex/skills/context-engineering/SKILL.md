---
name: context-engineering
description: Load and organize the project context required for a task, including AGENTS.md, boundaries, relevant workflows, domain documents, and selectively needed architecture or testing guidance.
---

# Context Engineering

## FrontMatter

- 실행 조건: 프로젝트 작업을 시작하거나 작업 범위가 변경될 때
- 실행하지 않는 조건: 단순한 질문이나 이미 확인된 컨텍스트 안에서의 짧은 답변
- Boundary 문서는 별도 Skill로 분리하지 않고 이 Skill에서 로드한다.

## Input Contract

- 사용자 요청
- 현재 작업 디렉터리
- 현재 브랜치와 작업 트리 상태
- 이미 확인된 문서와 결정 사항

## Steps

1. `AGENTS.md`를 확인한다.
2. `docs/boundary/`에서 관련 권한과 제약을 확인한다.
3. 요청의 목적과 대상 범위를 식별한다.
4. 관련 Workflow 문서를 확인한다.
5. 구현·디버깅·리뷰·요구사항 검증이면 대상 도메인 문서를 먼저 확인한다.
6. 테스트 변경이 포함되면 관련 테스트 문서를 확인한다.
7. 구조에 영향을 주는 경우에만 필요한 아키텍처 컨벤션을 탐색한다.
8. 관련 코드와 테스트를 탐색하고 필요한 컨텍스트 목록을 정리한다.

## Output Contract

- 대상 Workflow
- 대상 도메인과 범위
- 적용할 권한과 제약
- 읽은 문서 목록
- 탐색할 코드와 테스트 범위
- 미확정 사항

## Verification Gate

- [ ] AGENTS.md를 확인했는가?
- [ ] 관련 Boundary를 확인했는가?
- [ ] Workflow와 대상 범위를 식별했는가?
- [ ] 필요한 도메인·테스트 문서를 확인했는가?
- [ ] 관련 없는 문서를 무차별적으로 읽지 않았는가?

## Failure Policy

- 필수 문서를 찾지 못하면 경로를 확인하고 대체 근거를 탐색한다.
- 문서와 사용자 요청이 충돌하면 임의로 결정하지 않고 질문한다.
- 권한 범위를 판단할 수 없으면 변경을 시작하지 않는다.

## Exit Criteria

- 다음 작업에 필요한 컨텍스트와 범위가 명확하다.
- 미확정 사항이 해결되었거나 사용자에게 전달되었다.
- 다음 Skill이 사용할 입력이 정리되었다.

## Anti-Rationalization

- 문서 확인을 기억으로 대신하지 않는다.
- 관련 문서를 확인하지 않고 구현을 시작하지 않는다.
- 아키텍처 문서를 전부 읽거나 전혀 읽지 않는 양극단을 피하고 필요한 범위만 확인한다.
- 문서와 코드의 충돌을 임의로 해소하지 않는다.
