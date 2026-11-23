---
name: git-workflow-and-versioning
description: Safely manage branches, commits, pushes, and version history while protecting existing user changes and project Git rules.
---

# Git Workflow and Versioning

## FrontMatter

- 실행 조건: 구현·설정·문서 변경의 시작, commit 요청, push 요청
- 실행하지 않는 조건: 읽기 전용 탐색이나 단순 설명

## Input Contract

- 사용자 요청과 승인 범위
- 현재 브랜치
- 작업 트리 상태
- 원격 저장소 상태
- 프로젝트 Git Workflow 규칙

## Steps

1. 관련 Git Workflow와 Boundary를 확인한다.
2. 현재 브랜치와 작업 트리의 사용자 변경을 확인한다.
3. 필요한 경우 version, action, detail과 브랜치명을 확정한다.
4. 작업 브랜치와 변경 범위를 확인한다.
5. commit 전 diff, 파일 목록, 커밋 단위를 확인한다.
6. 커밋 메시지 형식과 목적을 검증한다.
7. push 요청이 있으면 원격 브랜치와 최신 상태를 확인한다.
8. 위험한 이력 변경은 대상과 복구 가능성을 확인한 뒤 수행한다.

## Output Contract

- 현재 브랜치와 원격 브랜치
- 포함·제외된 파일
- 커밋 목록과 목적
- push 결과
- 남은 미커밋 변경

## Verification Gate

- [ ] 관련 사용자 변경을 보호했는가?
- [ ] 올바른 작업 브랜치인가?
- [ ] commit 전 diff를 확인했는가?
- [ ] 커밋 메시지가 규칙에 맞는가?
- [ ] push 후 원격 커밋과 작업 트리를 확인했는가?

## Failure Policy

- 작업 트리에 사용자 변경이 있으면 덮어쓰지 않고 처리 방법을 확인한다.
- 인증·네트워크 오류는 원인을 보고하고 우회하지 않는다.
- 원격 변경이 확인되면 강제 push하지 않고 사용자에게 확인한다.
- commit과 push는 별도 요청으로 처리한다.

## Exit Criteria

- 승인된 범위만 변경·커밋·push되었다.
- 로컬과 원격의 브랜치 상태가 확인되었다.
- 남은 변경과 후속 작업이 보고되었다.

## Anti-Rationalization

- 구현이 끝났다는 이유로 자동 commit하지 않는다.
- commit 요청이 없는데 commit하지 않는다.
- push 요청이 없는데 push하지 않는다.
- main에 직접 변경하거나 이력을 임의로 재작성하지 않는다.
