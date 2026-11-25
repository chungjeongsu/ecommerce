---
name: order
description: 주문 생성, 주문 당시 상품 정보, 재고 예약, 주문 상태 전이와 취소·만료 규칙을 정의한다.
---

# Order 도메인

## Aggregate

`Order`가 Root Aggregate이며 `OrderItem`을 포함한다.

```text
Order
└─ OrderItem
```

OrderItem은 주문 당시 상품명, 옵션 조합, 단가와 수량을 직접 보존하므로 Product, Variant의 생명주기와 분리한다.

## 책임

- 주문자와 주문 항목 관리
- 주문 당시 상품 정보와 가격 보존
- 주문 총액 관리
- 주문 상태 전이와 취소·만료 판단
- 주문 생성 중 재고 예약 결과 반영

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

OrderItem
- 주문 항목의 수량은 1개 이상 100개 이하여야 한다.
- 주문 당시 단가와 주문 항목 금액은 `long`으로 관리한다.
- 주문 항목 금액은 주문 당시 단가와 수량을 곱해 계산한다.
- 주문 당시 상품명, 옵션 조합, 단가와 수량을 직접 보존한다.
- Order 생성 후 Variant, 상품명, 옵션 조합, 단가와 수량을 변경할 수 없다.

### Root Aggregate 불변식

Order
- 하나 이상의 OrderItem을 포함해야 한다.
- 하나의 Order에 동일한 Variant를 중복해서 포함할 수 없다.
- 주문 항목 컬렉션은 Order 생성 후 추가·삭제·교체할 수 없다.
- 주문 총액은 저장하지 않고 각 OrderItem 금액의 합으로 계산한다.
- 내부 주문 식별자 `id`는 Long 형태로 관리한다.
- 외부에 노출하는 `orderCode`는 UUID 형태로 고유해야 하며 생성 후 변경할 수 없다.
- 주문 생성 시각과 만료 시각은 외부에서 전달받고, 만료 시각은 생성 시각보다 이후여야 한다.
- 주문 내 재고 예약은 모두 성공하거나 모두 실패해야 한다.
- 결제 진행 중이거나 결과가 `UNKNOWN`이면 주문을 임의로 만료하거나 종료하지 않는다.
- 사전에 정의된 상태 전이만 허용한다.

OrderStatus
- 주문 생성과 재고 예약이 같은 트랜잭션에서 처리되므로 영속 상태로 `CREATED`를 저장하지 않는다.
- `READY → PAID`, `READY → CANCELLED`, `READY → EXPIRED` 전이만 허용한다.
- 재고 예약 실패 시 Order를 저장하지 않고 전체 트랜잭션을 롤백한다.

### Application 외부 불변식

#### CreateOrderUsecase

- 구매자만 주문을 생성할 수 있다.
- 주문자는 존재하는 사용자여야 한다.
- 동일 사용자는 서로 다른 주문에서 같은 Variant를 반복 주문할 수 있다.
- 판매 중인 상품의 Variant만 주문할 수 있다.
- 상품 정보와 가격을 조회해 주문 당시 정보를 담은 OrderItem을 생성한다.
- 주문 내 재고 예약은 원자적으로 모두 성공하거나 모두 실패해야 한다.
- 재고 예약 성공 시 `READY` 상태로 저장한다.
- 재고 예약 실패 시 Order를 저장하지 않고 전체 트랜잭션을 롤백한다.
- 부족한 모든 Variant의 식별자, 상품명과 옵션 조합을 결합한 표시 이름, 요청 수량과 가용 수량을 Result의 목록으로 반환한다.
- 품절 여부를 Variant 상태로 저장하지 않고 Inventory 조회 결과로 표현한다.

재고 부족은 일반 예외 응답과 분리된 주문 생성 실패 Result·Response로 반환한다.
재고 상태와 주문 요청이 충돌한 경우 HTTP `409 CONFLICT`를 반환한다.

주문 생성 Usecase의 성공·실패 결과는 Sealed Interface 다형성으로 표현한다.

```text
CreateOrderUsecaseResult
├─ CreateOrderSuccessResult
└─ CreateOrderFailureResult
   └─ shortages
```

- `CreateOrderSuccessResult`는 생성된 주문 식별자를 포함한다.
- `CreateOrderFailureResult`는 부족한 모든 Variant의 정보를 포함한다.
- Application은 Result를 반환하고 HTTP Response를 직접 생성하지 않는다.
- Presentation은 성공 Result를 `CreateOrderRes`, 실패 Result를 `CreateOrderFailureRes`로 변환한다.

```json
{
  "shortages": [
    {
      "variantId": 101,
      "displayName": "반팔 티셔츠 - 색상: 검정 / 사이즈: L",
      "requestedQuantity": 2,
      "availableQuantity": 0
    }
  ]
}
```

#### CancelOrderUsecase

- 구매자만 자신의 주문을 취소할 수 있다.
- 결제 진행 중이거나 결과가 `UNKNOWN`이면 취소·종료하지 않는다.

#### ExpireOrderUsecase

- 결제 기한이 지난 주문만 만료할 수 있다.
- 현재 시각은 Application에서 Domain으로 전달한다.
- 결제 진행 중이거나 결과가 `UNKNOWN`이면 만료하지 않는다.
- 만료된 주문의 재고 예약을 `RELEASED`로 전환한다.
- 결제 기한이 지난 주문은 Scheduler를 통해 주기적으로 처리할 수 있다.

#### UpdateOrderItemUsecase

- Product 또는 Variant가 변경·삭제되어도 기존 OrderItem에 보존한 상품 정보는 변경하지 않는다.
- 주문 항목 변경은 지원하지 않는다.
- 변경이 필요하면 결제 전에 기존 Order를 취소하고 새로운 Order를 생성한다.

## 상태

```text
READY → PAID
READY → CANCELLED
READY → EXPIRED
```

- `READY → PAID`: 결제 완료
- `READY → CANCELLED`: 주문 취소 또는 결제 최종 실패
- `READY → EXPIRED`: 결제 기한 초과
- 사전에 정의되지 않은 상태 전이는 허용하지 않는다.

## 주요 속성

| 객체 | 속성 |
| --- | --- |
| Order | id(Long), orderCode(UUID), buyerId, status, items, createdAt, expiresAt |
| OrderItem | id, orderId, variantId, productName, optionCombination, unitPrice(long), quantity |
| OrderStatus | READY, PAID, CANCELLED, EXPIRED |
