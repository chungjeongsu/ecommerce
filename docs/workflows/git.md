# Git 브랜치·Commit·Push Workflow

## 목적

구현 작업의 변경 내용을 기능 단위로 관리하고, 최신 `main`에서 작업 브랜치를 생성해 안전하게 원격 저장소에 반영한다.

이 Workflow는 새로운 기능 구현뿐 아니라 `chore`, `refactor`, `docs`처럼 별도 브랜치가 필요한 변경에도 적용한다.

## 적용 시점

다음 요청에는 이 Workflow를 적용한다.

- 사용자가 새로운 기능을 구현하자고 요청한 경우
- 사용자가 코드 구조를 변경하거나 리팩터링하자고 요청한 경우
- 사용자가 문서·설정·의존성 변경을 요청한 경우
- 사용자가 commit 또는 push를 명시적으로 요청한 경우

단순한 질문이나 코드 탐색만 수행하는 경우에는 브랜치를 생성하거나 commit·push를 수행하지 않는다.

## 브랜치 이름 결정

구현이나 변경을 시작하기 전에 다음 세 가지를 확인한다.

1. `version`: `v1`, `v2`처럼 양의 정수 버전
2. `action`: `feat`, `chore`, `refactor`, `docs` 중 하나
3. `detail`: 사용자의 설명을 의미가 유지되는 영문 kebab-case로 변환한 상세명

예시는 다음과 같다.

```text
사용자 설명: 재고 도메인 구현
version: v1
action: feat
detail: add-inventory-domain
브랜치: v1/feat/add-inventory-domain
```

- 사용자가 세 항목을 모두 제시하면 형식만 검증한다.
- 일부 항목이 없으면 `version` → `action` → `detail` 순서로 하나씩 확인한다.
- `detail`은 AI가 임의로 확정하지 않고 사용자의 확인을 받아 최종 브랜치명을 정한다.
- 브랜치명에는 공백, 한글, 특수문자를 사용하지 않는다.
- commit 타입과 브랜치 action은 별개의 값이다. 예를 들어 `feat` 브랜치에서 테스트만 추가하는 commit은 `test:`를 사용할 수 있다.

## 작업 브랜치 생성

브랜치 정보가 확정되면 최신 `main`에서 작업을 시작한다.

```text
현재 작업 상태 확인
        ↓
main으로 이동
        ↓
git pull origin main
        ↓
새 작업 브랜치 생성 및 이동
        ↓
Implement Workflow 수행
```

실행 기준:

- 먼저 작업 트리의 변경 사항과 현재 브랜치를 확인한다.
- 추적되지 않은 변경이나 미커밋 변경이 있으면 덮어쓰지 않고 사용자에게 처리 방법을 확인한다.
- `main`으로 이동한 뒤 `git pull origin main`을 수행한다.
- `git switch --create {version}/{action}/{detail}`로 새 브랜치를 생성한다.
- `reset --hard`, 강제 checkout, 임의의 stash 삭제로 사용자 변경을 폐기하지 않는다.
- 브랜치 생성이 완료된 뒤에만 구현 작업을 시작한다.

## 구현과 Commit의 순서

구현은 선택된 기능 Workflow(`/impl`, `/debug`, `/refactor`, `/docs` 등)에 따라 진행한다.

- Implement Workflow의 준비·TDD·검증·종료 조건을 먼저 완료한다.
- 구현이 완료되기 전에는 사용자가 요청하지 않은 commit을 수행하지 않는다.
- 테스트 실패나 미확정 요구사항이 남아 있으면 commit 전에 사용자에게 보고한다.
- commit 요청이 없으면 변경 사항은 작업 트리에 유지한다.

## Commit 규칙

사용자가 commit을 요청하면 변경 내용을 한 번에 모두 commit하지 않고, 나눌 수 있는 논리적 작업 단위별로 commit한다.

- 하나의 commit은 하나의 목적을 가진다.
- 도메인 모델과 해당 테스트처럼 분리할 경우 의미가 깨지는 변경은 하나의 단위로 묶을 수 있다.
- 독립적인 도메인·계층·문서 변경은 각각 별도 commit으로 나눈다.
- 관련 없는 사용자 변경은 stage하지 않는다.
- commit 전 `git diff`와 `git status`로 포함 파일을 확인한다.
- 기능 구현이 끝났다는 이유만으로 자동 commit하지 않는다.

Commit 메시지는 다음 형식을 사용한다.

```text
{type}: {한국어로 작성한 간결한 변경 설명}
```

허용하는 기본 타입은 다음과 같다.

- `feat`: 새로운 기능
- `fix`: 오류 수정
- `refactor`: 동작을 유지하는 구조 개선
- `test`: 테스트 추가·수정
- `chore`: 설정·의존성·개발 보조 작업
- `docs`: 문서 변경

예시는 다음과 같다.

```text
feat: 재고 예약 도메인 구현
test: 상품 Variant 상태 전이 테스트 추가
docs: Flyway 마이그레이션 규칙 문서화
chore: Flyway 의존성 설정 추가
```

- 메시지는 한 줄로 작성한다.
- 구현 내용이 드러나도록 작성하고 불필요한 내부 파일명 나열은 피한다.
- 여러 commit이 필요한 경우 각 commit의 목적이 서로 겹치지 않도록 한다.

## Push 규칙

사용자가 commit 완료 후 push를 명시적으로 요청한 경우에만 원격 저장소에 push한다.

```text
git push origin {현재 브랜치명}
```

- push 대상은 현재 작업 브랜치이며 `main`에 직접 push하지 않는다.
- commit 직후 자동으로 push하지 않는다.
- 강제 push(`--force`, `--force-with-lease`)를 사용하지 않는다.
- push 전에 현재 브랜치, 최근 commit, 작업 트리 상태를 다시 확인한다.
- push 성공 후 원격 브랜치와 commit 식별자를 확인한다.
- PR 생성은 push와 별도의 작업이며 사용자가 요청한 경우에만 진행한다.
- push가 성공하면 바로 사용할 수 있는 짧은 PR 메시지를 Markdown 형식으로 함께 제공한다.
- 인증·네트워크·권한 오류가 발생하면 원인을 보고하고 우회하거나 강제 실행하지 않는다.

PR 메시지는 변경 사항과 검증 결과만 간략히 포함한다.

```markdown
## 변경 사항

- Flyway 마이그레이션 규칙 문서 추가
- Git Workflow 및 문서 라우팅 규칙 보완

## 검증

- 문서 링크 및 `git diff --check` 확인
```

## 완료 보고

Workflow 완료 시 다음 내용을 보고한다.

- 생성한 브랜치명
- 수행한 commit 목록과 각 commit 목적
- push 여부와 원격 브랜치
- 검증한 테스트 또는 문서
- 남아 있는 미커밋 변경과 후속 작업
- push 성공 시 간략한 PR 메시지(Markdown)
## 계층별 커밋 분리 규칙

구현 완료 후 commit을 요청받으면 가능한 한 하나의 목적과 하나의 대상 단위로 나누어 커밋한다.

예시:

```text
feat: 재고 예약 엔티티와 테스트 구현
feat: 재고 엔티티와 테스트 구현
feat: 재고 저장소와 마이그레이션 구현
docs: 재고와 주문 도메인 규칙 정리
docs: 엔티티와 테스트 규칙 정리
docs: 구현과 Flyway 워크플로우 정리
```

- Domain Entity와 상태 Enum은 해당 Entity 테스트와 함께 묶는다.
- `Inventory`와 `InventoryReservation`처럼 독립적인 Aggregate 또는 내부 객체는 별도 commit으로 분리한다.
- Repository, Flyway, Repository 통합 테스트는 하나의 Persist 목적 단위로 묶는다.
- 서로 다른 문서 목적은 별도 `docs` commit으로 분리한다.
- commit 전 각 묶음의 파일을 확인하고, 관련 없는 변경을 포함하지 않는다.

## 구현 완료와 Commit의 관계

- `/impl`의 계층별 구현과 검증이 끝난 뒤에만 commit한다.
- 일부 계층의 테스트가 실행되지 않았다면 해당 사실을 commit 전에 보고한다.
- 컴파일 성공만으로 구현 완료를 판단하지 않는다.
- Domain, Application, Persist, Presentation 변경은 각 계층의 테스트 범위가 충족되었는지 확인한다.

## PR 메시지 작성

push가 완료되면 다음 형식의 짧은 Markdown PR 메시지를 제공한다.

```markdown
## 변경 사항

- 재고 예약 및 재고 Aggregate 구현
- Repository와 Flyway 스키마 추가
- 도메인·테스트·Git Workflow 문서 정리

## 검증

- `compileJava`
- `compileTestJava`
- Repository 통합 테스트 결과 또는 실행 불가 사유
```
