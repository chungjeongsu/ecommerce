---
name: product
description: 상품, 옵션, 옵션값, Variant 구성과 판매 상태 및 가격 규칙을 정의한다.
---

# Product 도메인

## Aggregate

`Product`가 Root Aggregate이며 다음 객체를 포함한다.

```text
Product
├─ ProductStatus
├─ Option
├─ OptionValue
└─ Variant
```

Inventory는 재고 정합성과 예약 생명주기가 독립적이므로 Product Aggregate에 포함하지 않는다.

## 책임

- 상품의 기본 정보와 판매 상태 관리
- 상품별 Option과 OptionValue 관리
- OptionValue 조합을 통한 Variant 관리
- Variant별 가격과 옵션 조합 관리

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

Option
- 이름은 1자 이상 10자 이하여야 한다.

OptionValue
- 이름은 1자 이상 10자 이하여야 한다.

Variant
- 가격은 0원 이상이어야 한다.
- 상태는 `ACTIVE` 또는 `STOPPED`이다.
- 상태는 `ACTIVE ↔ STOPPED` 전이만 허용한다.

### Root Aggregate 불변식

Product
- 상품명은 필수이며 비어 있거나 공백일 수 없다.
- 상품명은 최소 3자, 최대 12자여야 한다.
- 상품명과 설명은 앞뒤 공백을 제거한 후 저장하며, 내부 공백은 허용한다.
- 상품 설명은 선택 입력이다.
- 상품 설명을 입력하는 경우 비어 있거나 공백일 수 없으며, 최대 100자여야 한다.
- 하나의 Product 내부에서 대소문자를 구분하지 않고 Option 이름을 중복해서 등록할 수 없다.
- 하나의 Option 내부에서 대소문자를 구분하지 않고 OptionValue 이름을 중복해서 등록할 수 없다.
- Product의 모든 Option은 하나 이상의 OptionValue를 가져야 한다.
- Variant는 Product의 모든 Option에서 각각 하나의 OptionValue를 포함해야 한다.
- 하나의 Variant에 동일한 Option의 OptionValue를 중복해서 포함할 수 없다.
- 동일한 OptionValue 조합을 가진 Variant를 중복해서 등록할 수 없다.
- Variant가 하나라도 존재하는 Product에는 새로운 Option을 직접 추가할 수 없다. 새로운 Option이 필요하면 해당 Option을 포함한 새로운 Product로 등록한다.
- 기존 Option에는 새로운 OptionValue를 추가할 수 있다. 새 OptionValue를 사용하는 Variant는 판매자가 별도로 생성한다.
- 사용 중인 OptionValue는 삭제할 수 없다.
- 어떤 Variant에서도 사용하지 않고 이력도 없는 OptionValue는 삭제할 수 있다.
- OptionValue가 모두 삭제되었고, 어떤 Variant에서도 사용하지 않으며 이력도 없는 Option은 삭제할 수 있다.
- 예약 중이거나 주문 이력이 있는 Variant의 옵션 조합은 변경할 수 없다.
- 옵션 조합을 변경해야 하는 경우 기존 Variant를 `STOPPED`로 변경하고, 변경된 옵션 조합을 새로운 Variant로 추가한다.
- `STOPPED` 상태의 Variant는 신규 주문과 재고 예약 대상이 될 수 없다.
- Variant를 `STOPPED`로 변경해도 기존 재고 예약의 확정·해제를 방해하지 않는다.
- `STOPPED`에서 `ACTIVE`로 전환할 때 Variant의 옵션 조합과 가격 불변식을 확인한다.

ProductStatus
- 상태는 `ON_SALE` 또는 `STOPPED`이다.
- `STOPPED` Product는 `ACTIVE` Variant가 하나 이상이고 Product 불변식을 만족할 때만 `ON_SALE`로 전환할 수 있다.
- 재고 보유 여부는 Product 상태 전환 조건으로 사용하지 않는다.
- Product와 Variant의 판매 중지는 기존 재고 예약의 확정·해제를 방해하지 않는다.

### Application 외부 불변식

#### CreateProductUsecase

- 요청한 판매자가 상품 등록 권한을 가져야 한다.
- 같은 판매자가 동일한 상품명을 사용할 수 없다.
- `STOPPED` Product의 상품명도 중복 검사 대상에 포함한다.

#### UpdateProductUsecase

- 요청한 판매자가 해당 Product의 소유자여야 한다.
- 상품명 변경 시 판매자별 중복 규칙을 만족해야 한다.
- Option과 OptionValue의 이름 변경은 주문 이력이 있어도 허용한다. 과거 주문은 OrderItem 스냅샷을 사용한다.

#### AddVariantUsecase

- 요청한 판매자가 해당 Product의 소유자여야 한다.
- 연결된 OptionValue가 해당 Product에 속해야 한다.
- 동일한 OptionValue 조합의 Variant가 없어야 한다.

#### UpdateVariantPriceUsecase

- 요청한 판매자가 해당 Product의 소유자여야 한다.
- 가격 변경은 신규 주문부터 적용한다.
- 이미 생성된 OrderItem의 단가와 주문 총액은 재계산하지 않는다.
- 결제 대기 중인 주문도 주문 생성 당시의 금액으로 결제한다.
- 재고 예약에는 가격 변경을 반영하지 않는다.

#### ChangeVariantStatusUsecase

- 요청한 판매자가 해당 Product의 소유자여야 한다.
- `ACTIVE ↔ STOPPED` 전이만 허용한다.
- 기존 재고 예약을 취소하거나 변경하지 않는다.

#### ChangeVariantStatusUsecaseFacade

- 연결된 Inventory가 있으면 Variant를 `STOPPED`로 전환하기 전에 InventoryApi로 해당 Inventory를 `CLOSING`으로 전환한다.
- 연결된 Inventory가 없으면 Inventory 전환 없이 Variant를 `STOPPED`로 전환할 수 있다.
- Inventory가 존재하지만 `CLOSING`으로 전환되지 않으면 Variant를 `STOPPED`로 전환할 수 없다.
- Variant와 Inventory 상태 변경은 하나의 트랜잭션으로 처리하고, 하나라도 실패하면 전체를 롤백한다.
- Inventory의 기존 예약은 임의로 취소하지 않는다.

#### DeleteProductUsecase

- 이력이 있는 Product는 물리 삭제하지 않고 `STOPPED`로 전환한다.
- `STOPPED` Product는 신규 주문과 재고 예약을 차단하고 기존 이력을 보존한다.
- 물리 삭제와 아카이빙은 현재 범위에서 고려하지 않는다.

#### DeleteVariantUsecase

- 이력이 있는 Variant는 물리 삭제하지 않고 `STOPPED`로 전환한다.
- `STOPPED` Variant는 신규 주문과 재고 예약을 차단하고 기존 이력을 보존한다.
- 물리 삭제와 아카이빙은 현재 범위에서 고려하지 않는다.

#### ResumeProductUsecase

- 요청한 판매자가 해당 Product의 소유자여야 한다.
- `ACTIVE` Variant가 하나 이상이어야 한다.
- Product 불변식을 만족해야 한다.
- 재고 보유 여부는 Product 전환 조건으로 확인하지 않는다.


## 상태

```text
ON_SALE ↔ STOPPED
```

판매 상태 변경은 해당 상품의 소유 판매자만 수행할 수 있다.

`STOPPED` Product는 `ACTIVE` Variant가 하나 이상이고 Product 불변식을 만족하면 다시 `ON_SALE`로 전환할 수 있다. 재고 보유 여부는 Product의 전환 조건으로 확인하지 않으며, 실제 주문 가능 여부는 Inventory 도메인이 판단한다.

## 주요 속성

| 객체 | 속성 |
| --- | --- |
| Product | id, sellerId, name, description, status, options, variants |
| Option | id, productId, name, optionValues |
| OptionValue | id, optionId, name |
| Variant | id, productId, price, status, optionValues |
| ProductStatus | ON_SALE, STOPPED |
| VariantStatus | ACTIVE, STOPPED |
