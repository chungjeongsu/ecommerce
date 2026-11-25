---
name: settlement
description: 정산 도메인의 현재 보류 상태와 향후 모델링할 객체 및 규칙 범위를 정의한다.
---

# Settlement 도메인

Settlement는 현재 구현과 상세 모델링을 보류한다.

구현과 모델링이 시작되면 Product, Inventory, Order, Payment, User 문서와 동일하게 다음 구조로 작성한다.

```text
Aggregate
책임
Aggregate와 불변식
├─ 객체 내부 불변식
├─ Root Aggregate 불변식
└─ Application 외부 불변식
   └─ Usecase별 규칙
상태
주요 속성
```

정산 기능을 시작할 때 다음 객체와 Aggregate 경계를 다시 정의한다.

- SettlementTarget
- Settlement
- SettlementStatus
- CommissionPolicy

정산 대상의 생성 조건, 수수료 계산, 정산 확정과 지급 상태의 생명주기는 별도 도메인 요구사항으로 확정한 뒤 문서화한다.
