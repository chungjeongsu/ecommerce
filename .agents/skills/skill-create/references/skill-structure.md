# Skill 구조

## 기본 구조

단순한 Skill은 `SKILL.md` 하나로 구성한다.

```text
.agents/skills/{skill-name}/
└─ SKILL.md
```

상세 자료가 필요할 때만 supporting resource를 추가한다.

```text
.agents/skills/{skill-name}/
├─ SKILL.md
└─ references/
   └─ {topic}.md
```

## 자동 발견

독립적으로 발견되어야 하는 Skill은 `.agents/skills/` 바로 아래에 별도 디렉터리로 둔다.

`git-commit/SKILL.md`는 `git-workflow`의 하위 문서가 아니라 `git-commit`이라는 독립 Skill로 구성한다.

하위 디렉터리의 `SKILL.md`는 부모 Skill의 일부로 자동 처리된다고 가정하지 않는다.

## 배치 기준

- 실행 지침은 `SKILL.md`에 둔다.
- 조건부 상세 정보는 `references/`에 둔다.
- 반복 실행이 필요한 자동화는 `scripts/`에 둔다.
- 생성 결과에 포함할 파일은 `assets/`에 둔다.
- 사용하지 않는 디렉터리와 placeholder 파일은 만들지 않는다.
