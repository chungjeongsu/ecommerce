---
name: payment
description: 결제 승인, 멱등성, PG 거래 결과와 결제 상태 생명주기 규칙을 정의한다.
---

# Payment 도메인

## Aggregate

`Payment`가 Root Aggregate다.

```text
Payment
└─ PaymentStatus
```

Payment는 PG 통신 결과와 결제 상태의 생명주기를 관리하며, 주문과 재고를 직접 소유하지 않는다.

## 책임

- 결제 대상 주문과 요청 금액 관리
- 결제 멱등키와 PG 거래 식별 정보 관리
- 결제 승인 상태 전이 관리
- 승인 결과와 결과 불확실 상태 관리
- 결제 결과에 따른 주문·재고 후속 처리의 기준 제공

## Aggregate와 불변식

도메인 테스트는 Aggregate 내부 객체부터 Root Aggregate 순서로 작성한다.

```text
객체 내부 불변식
        ↓
Root Aggregate 불변식
        ↓
Application 외부 불변식
```

### 객체 내부 불변식

Payment
- 결제마다 고유한 식별자와 멱등키를 가진다.
- 하나의 Order에 여러 Payment가 존재할 수 있으므로 Payment마다 고유한 `pgOrderId`를 생성한다.
- `pgOrderId`는 Toss PG에 전달하는 가맹점 주문번호이며 다른 Payment와 중복할 수 없다.
- PG 승인 요청 재시도를 위해 `pgApprovalIdempotencyKey`를 Payment마다 생성한다.
- 결제 요청 금액은 0 이상이어야 한다.

### Root Aggregate 불변식

Payment
- 결제 상태의 사전에 정의된 전이만 허용한다.
- 허용되지 않은 상태 전이는 거부한다.
- 동일 결제에 대한 중복 승인을 방지한다.
- 결과가 불확실한 결제를 임의로 실패 처리하지 않는다.

PaymentStatus
- `READY → IN_PROGRESS` 전이만 허용한다.
- 결제 기한이 지나면 `READY → EXPIRED`로 전이한다.
- `IN_PROGRESS → DONE`, `ABORTED`, `UNKNOWN` 전이만 허용한다.
- PG 승인 유효 시간이 지나면 `IN_PROGRESS → EXPIRED`로 전이한다.
- `UNKNOWN → DONE`, `ABORTED`, `EXPIRED` 전이만 허용한다.
- `DONE`, `ABORTED`, `EXPIRED`는 최종 상태이며 다른 상태로 전이할 수 없다.

### Application 외부 불변식

#### CreatePaymentUsecase

- 구매자만 결제를 요청할 수 있다.
- Payment 생성은 Order 도메인이 아니라 Payment 도메인의 Usecase가 담당한다.
- OrderApi를 통해 결제 대상 주문의 상태·총액·재고 예약 완료 여부를 확인한다.
- 결제 대상 Order가 존재해야 한다.
- 재고 예약이 완료된 주문만 결제할 수 있다.
- 결제 요청 금액은 Order 총액과 일치해야 한다.
- 완료·취소·만료된 주문은 결제할 수 없다.
- 동일한 Order에 `DONE` 상태의 Payment가 있으면 새로운 Payment를 생성할 수 없다.
- 검증이 완료된 Payment는 `READY` 상태로 저장한다.
- Payment 생성 후 `orderId`, `amount`, `pgOrderId`, `pgApprovalIdempotencyKey`는 변경할 수 없다.

#### ApprovePaymentUsecase

- 동일 결제에 대한 중복 승인을 방지해야 한다.
- 외부 PG 호출 중 DB 트랜잭션을 유지하지 않는다.
- `READY` Payment를 `IN_PROGRESS`로 변경하고 커밋한 뒤 외부 PG를 호출한다.
- PG 승인 결과의 금액과 Payment에 저장된 금액이 일치하는지 확인한다.
- 금액이 일치하지 않으면 Payment를 `DONE`으로 반영하지 않고 결제 정합성 오류로 처리한다.
- 결제 상태의 최종 기준은 PG 결과로 판단한다.
- PG 결과를 확인할 수 없으면 `UNKNOWN`으로 관리한다.

PG 승인 성공 결과 반영은 하나의 트랜잭션으로 처리한다.

```text
Payment = DONE
→ OrderApi로 Order = PAID 반영
→ InventoryApi로 예약 확정 및 재고 차감
→ 전체 커밋
```

- Payment는 Order와 Inventory의 내부 구현을 직접 참조하지 않는다.
- Order와 Inventory의 공개 API를 통해 후속 상태 변경을 요청한다.
- 후속 상태 변경 중 하나라도 실패하면 결과 반영 트랜잭션 전체를 롤백한다.
- 로컬 결과 반영 트랜잭션이 실패하면 `paymentId`, `orderId`, `pgOrderId`, `pgPaymentKey`, PG 상태, 실패 단계를 포함한 `ERROR` 로그를 남긴다.
- 로컬 트랜잭션 실패만으로 Payment를 `ABORTED`로 변경하지 않는다.
- Scheduler는 PG 상태를 재조회한 뒤 확인된 결과에 따라 Order와 Inventory 후속 처리를 재시도한다.

PG 승인 최종 실패 결과 반영도 하나의 트랜잭션으로 처리한다.

```text
Payment = ABORTED
→ OrderApi로 Order = CANCELLED 반영
→ InventoryApi로 RESERVED 예약 해제
→ 전체 커밋
```

- Payment는 Order와 Inventory의 공개 API만 사용한다.
- 주문 취소 또는 예약 해제 중 하나라도 실패하면 결과 반영 트랜잭션 전체를 롤백한다.

`ApprovePaymentUsecaseFacade`가 다음 흐름을 조정한다.

```text
Payment 상태 변경 트랜잭션
        ↓ 커밋
PgClient 호출
        ↓
PG 결과 반영 트랜잭션
```

- `ApprovePaymentUsecaseFacade`는 트랜잭션 경계와 PG 호출 순서를 조정한다.
- `ApprovePaymentUsecaseImpl`은 Payment 검증·상태 변경·결과 반영을 수행한다.
- `PgClient`는 외부 PG 연동 계약이다.
- PG별 구현체는 `TossPgClientImpl`처럼 `PgClient`를 구현한다.

#### RecoverPaymentUsecase

- `UNKNOWN` 결제는 동일한 `paymentKey`, `pgOrderId`, `amount`, `pgApprovalIdempotencyKey`를 사용해 승인 API를 재호출한다.
- 동일 멱등키의 승인 재호출은 중복 승인을 발생시키지 않아야 한다.
- 재호출 결과가 처리 중이면 PG 조회 또는 Webhook으로 최종 결과를 확인한다.
- 최종 결과 확인 전 결제를 임의로 실패 처리하지 않는다.
- Payment가 `UNKNOWN`인 동안 Order는 `READY`를 유지한다.
- Payment가 `UNKNOWN`인 동안 Inventory의 `RESERVED` 예약은 유지한다.
- Payment 결과가 확정되기 전에는 Order 만료와 Inventory 예약 해제를 수행하지 않는다.
- Webhook으로 받은 PG 결과도 동일한 Payment 상태 전이 규칙을 사용한다.
- 이미 최종 상태인 Payment에 동일하거나 늦게 도착한 결과는 후속 처리를 반복하지 않는다.
- Webhook API는 PG에서 전달되는 모든 Webhook 이벤트를 수신한다.
- 이벤트별 상세 처리 규칙과 지원 범위는 별도 요구사항으로 정의할 때 확정한다.
- 수신 시 Webhook 서명은 검증하고, 검증된 이벤트만 기록·처리 대상으로 삼는다.

#### RetryPaymentUsecase

- 기존 Payment가 `EXPIRED` 또는 재시도할 수 없는 종료 상태여야 한다.
- Order가 `READY` 상태이고 InventoryReservation이 `RESERVED` 상태여야 한다.
- 동일한 Order에 `DONE` 상태의 Payment가 없어야 한다.
- 기존 Payment를 재사용하지 않고 새로운 Payment를 생성한다.
- 새로운 `pgOrderId`와 `pgApprovalIdempotencyKey`를 생성한다.
- 새 Payment의 결제 성공 시 기존 Order의 예약을 확정한다.
- 기존 Payment의 이력은 변경하지 않고 보존한다.

#### ExpirePaymentUsecase

- Scheduler가 결제 기한이 지난 Order와 Payment를 조회한다.
- `READY` Payment는 우리 결제 기한이 지나면 `EXPIRED`로 전환한다.
- `IN_PROGRESS` 또는 `UNKNOWN` Payment는 PG API로 현재 상태를 조회한다.
- PG 조회 결과가 결제 성공(`DONE`)이면 일반 승인 성공 흐름과 동일하게 처리한다.
- PG 조회 결과가 최종 실패(`ABORTED`)이면 일반 승인 실패 흐름과 동일하게 처리한다.
- PG가 결제 만료를 확인한 경우에만 `EXPIRED`로 전환한다.
- PG 결과가 불명확하면 Payment, Order, InventoryReservation 상태를 유지한다.
- Payment 만료와 함께 Order를 `EXPIRED`로 전환하고 InventoryReservation을 `RELEASED`로 변경한다.
- Payment, Order, Inventory의 상태 변경은 하나의 트랜잭션으로 처리한다.
- 로컬 후속 처리 실패 시 PG가 이미 `DONE`이면 승인 재요청이 아니라 결과 반영 트랜잭션을 재시도한다.

## 상태

```text
READY → IN_PROGRESS
READY → EXPIRED
IN_PROGRESS → DONE
IN_PROGRESS → ABORTED
IN_PROGRESS → UNKNOWN
IN_PROGRESS → EXPIRED
UNKNOWN → DONE
UNKNOWN → ABORTED
UNKNOWN → EXPIRED
```

## 주요 속성

| 객체 | 속성 |
| --- | --- |
| Payment | id, orderId, amount, status, pgOrderId, pgPaymentKey, pgTransactionKey, pgApprovalIdempotencyKey, createdAt, updatedAt |
| PaymentStatus | READY, IN_PROGRESS, UNKNOWN, DONE, ABORTED, EXPIRED |
