# Inventory 도메인

## Aggregate

`Inventory`가 Root Aggregate이며 `InventoryReservation`을 포함한다.

```text
Inventory
└─ InventoryReservation
   └─ InventoryReservationStatus
```

Inventory와 Product는 분리한다. 재고 정합성과 예약 생명주기는 상품 판매 상태와 독립적으로 관리되어야 한다.

## 책임

- Variant별 총 재고와 가용 재고 관리
- 주문별 재고 예약 관리
- 예약 재고의 확정 차감과 해제
- 예약 상태 전이 관리

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

InventoryReservation
- 예약 수량은 1 이상이어야 한다.
- 예약 생성 후 주문 식별자와 예약 수량은 변경할 수 없다.

### Root Aggregate 불변식

Inventory
- 재고 수량은 음수가 될 수 없다.
- 상태는 `ACTIVE`, `CLOSING`, `CLOSED` 중 하나다.
- `ACTIVE → CLOSING → CLOSED` 전이를 기본으로 하며, 예약이 모두 종료된 `CLOSED`는 `ACTIVE`로 재개할 수 있다.
- 예약 중 수량은 `RESERVED` 상태인 InventoryReservation의 수량 합계로 계산한다.
- `CONFIRMED`와 `RELEASED` 상태의 예약은 예약 중 수량에 포함하지 않는다.
- 예약 총량은 총 재고를 초과할 수 없다.
- 가용 재고는 총 재고에서 `RESERVED` 상태 예약 수량을 뺀 값이다.
- 동일 주문에서 동일 재고를 중복 예약할 수 없다.
- 예약이 존재하는 재고는 삭제할 수 없다.
- 예약 수량 계산·초과 검증·예약 저장은 동시성 상황에서도 원자적으로 처리한다.

예약 생성 시 `totalQuantity`는 유지하고 `RESERVED` 예약 수량에 포함한다. 예약 해제 시 예약 중 수량에서 제외하지만 `totalQuantity`는 유지한다. 예약 확정 시 확정 수량만큼 `totalQuantity`를 차감한 뒤 `CONFIRMED` 상태로 변경한다.

InventoryReservationStatus
- `RESERVED → CONFIRMED` 또는 `RESERVED → RELEASED` 전이만 허용한다.
- `CONFIRMED`, `RELEASED`는 최종 상태다.

InventoryStatus
- `ACTIVE`는 입고·조정·예약이 가능한 상태다.
- `CLOSING`은 신규 예약과 일반적인 재고 변경을 금지하고 기존 예약의 확정·해제만 허용하는 상태다.
- `CLOSING`에서도 기존 `RESERVED` 예약의 `RELEASED` 전환은 허용한다.
- 예약 해제 시 예약 중 수량에서 제외하고 `totalQuantity`는 변경하지 않는다.
- `CONFIRMED` 예약의 취소·재고 복구는 반품·환불에 따른 별도 흐름으로 처리한다.
- `CLOSED`는 모든 예약이 종료된 상태다. 남은 가용 재고는 보존할 수 있다.
- `CLOSED → ACTIVE` 전환은 모든 예약이 종료된 경우 허용한다.
- `CLOSED`에서는 예약 확정·해제와 일반 재고 변경을 수행하지 않는다.
- `CLOSED`에서 허용하는 상태 변경은 `ACTIVE` 재개뿐이다.

### Application 외부 불변식

#### CreateInventoryUsecase

- 요청한 판매자가 재고를 생성할 권한을 가져야 한다.
- 연결된 Variant가 존재해야 한다.
- 하나의 Variant에는 하나의 Inventory만 연결할 수 있다.
- 해당 Variant에 Inventory가 이미 존재하면 새로 생성하지 않고 기존 Inventory를 재사용한다.
- `STOPPED` Variant의 Inventory는 판매 재개를 위해 생성할 수 있으며, `CLOSED` 상태로 시작한다.
- Variant가 `ACTIVE`로 전환되면 기존 Inventory를 `ACTIVE`로 재사용한다.

#### UpdateInventoryUsecase

- 요청한 판매자가 해당 재고를 변경할 권한을 가져야 한다.
- 재고 증가는 허용한다.
- 예약 중인 수량보다 적은 총 재고로 변경할 수 없다.
- 재고 감소로 인해 기존 예약을 취소하거나 수량을 변경하지 않는다.

#### ReserveInventoryUsecase

- ProductApi를 통해 Variant의 판매 가능 상태를 확인한다.
- 판매 중지 상품 또는 `STOPPED` Variant에 대한 신규 예약은 생성할 수 없다.
- Inventory는 ProductStatus나 VariantStatus를 직접 참조하지 않는다.
- `ACTIVE` Inventory만 신규 예약을 생성할 수 있다.
- 하나의 주문에 속한 재고 예약은 모두 성공하거나 모두 실패해야 한다.
- 여러 Inventory 중 하나라도 예약에 실패하면 이미 생성된 해당 주문의 예약을 모두 해제하고 전체 예약을 실패 처리한다.
- 동일 주문·주문 항목·Inventory에 이미 예약이 있으면 중복 예약하지 않고 비즈니스 예외를 반환한다.
- 현재는 별도의 멱등키 기반 재처리 규칙을 두지 않는다.

#### ConfirmInventoryReservationUsecase

- 결제 최종 성공이 확인된 예약만 확정할 수 있다.
- 확정 처리 후 `CLOSING` Inventory에 `RESERVED` 예약이 남아 있지 않으면 `CLOSED`로 자동 전환한다.

#### ReleaseInventoryReservationUsecase

- 주문 취소·만료 또는 결제 최종 실패가 확인된 예약만 해제할 수 있다.
- 결제 진행 중이거나 `UNKNOWN`이면 임의로 예약을 해제할 수 없다.
- 해제 처리 후 `CLOSING` Inventory에 `RESERVED` 예약이 남아 있지 않으면 `CLOSED`로 자동 전환한다.
- 예약 상태 변경과 Inventory 상태 전환은 같은 트랜잭션에서 처리한다.

#### DeleteInventoryUsecase

- Inventory 물리 삭제는 현재 범위에서 고려하지 않는다.
- 운영 종료는 `CLOSING`과 `CLOSED` 상태 전환으로 처리한다.

#### CloseInventoryUsecase

- `CLOSING` 상태에서는 신규 예약을 생성할 수 없다.
- 기존 예약은 결제 결과에 따라 확정하거나 해제한다.
- 예약이 모두 종료되면 현재 재고 수량과 관계없이 `CLOSED`로 전환할 수 있다.
- 이력이 있는 Inventory는 `CLOSED` 상태로 보존한다.

#### ResumeInventoryUsecase

- 요청한 판매자가 해당 Inventory와 연결된 Variant의 소유자여야 한다.
- 연결된 Variant가 `ACTIVE` 상태여야 한다.
- `CLOSED` Inventory를 `ACTIVE`로 전환한다.
- 기존 재고 수량과 재고 이력을 유지한다.

## 상태

```text
RESERVED → CONFIRMED
RESERVED → RELEASED

ACTIVE → CLOSING
CLOSING → CLOSED
CLOSED → ACTIVE
```

- `CONFIRMED`, `RELEASED`는 최종 상태다.
- 결제 최종 성공 시 예약을 확정한다.
- 주문 취소·만료 또는 결제 최종 실패 시 예약을 해제한다.
- 결제 상태가 진행 중이거나 `UNKNOWN`이면 임의로 예약을 해제하지 않는다.

## 주요 속성

| 객체 | 속성 |
| --- | --- |
| Inventory | id, variantId, totalQuantity, status, reservations |
| InventoryReservation | id, inventoryId, orderId, orderItemId, quantity, status |
| InventoryReservationStatus | RESERVED, CONFIRMED, RELEASED |
| InventoryStatus | ACTIVE, CLOSING, CLOSED |
