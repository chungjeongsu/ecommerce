---
name: domain-convention
description: 도메인 객체, Aggregate, 불변식과 internal/domain 계층의 코드 작성 규칙을 정의한다.
---

# Domain 코드 컨벤션

## 책임

`internal/domain`은 모듈의 핵심 비즈니스 개념과 그 불변식을 관리한다.

- 도메인 객체의 상태와 행위를 정의한다.
- 객체와 Aggregate의 불변식을 보호한다.
- 상태 변경이 유효한지 객체 내부에서 판단한다.

유스케이스 실행 순서와 트랜잭션은 Application에서, 외부 시스템 연동은 Infra에서, 저장과 조회는 Persist에서 담당한다.

## 구조

```text
{module}/internal/domain/
├─ entity/
├─ vo/
├─ service/
└─ ...
```

실제 책임이 있을 때만 하위 패키지를 생성한다. 도메인 객체 하나를 형식적으로 여러 패키지로 나누지 않는다.

## 도메인 객체 명명

도메인 객체의 이름에는 기술적 역할을 나타내는 접미사를 붙이지 않는다.

- `Entity`, `Vo`, `Service`, `Event` 접미사를 사용하지 않는다.
- 객체가 표현하는 도메인 개념 자체를 이름으로 사용한다.
- 객체의 역할은 패키지와 책임으로 구분한다.

```text
Order
Money
Inventory
OrderCancellationPolicy
PaymentAmountValidator
OrderCancelled
```

## Aggregate와 Root Aggregate

Aggregate는 하나의 일관성 경계로 묶이는 도메인 객체의 집합이다. 외부에서는 Aggregate Root를 통해서만 Aggregate를 사용한다.

- Root Aggregate는 내부 객체의 생성과 변경을 관리한다.
- 외부에서 Aggregate 내부 객체를 직접 변경하지 않는다.
- Aggregate 내부 객체의 식별자나 상태를 외부에 불필요하게 노출하지 않는다.
- Aggregate 간 참조는 내부 객체가 아니라 필요한 식별자나 공개된 행위를 기준으로 한다.
- Aggregate 간 협력과 유스케이스 조합은 Application이 담당한다.

현재 프로젝트의 Root Aggregate와 하위 객체, 생명주기와 관계는 각 도메인의 문서를 기준으로 한다. 도메인 구조를 임의로 추가하거나 변경하지 않는다.

- 객체, 행위, 속성, 불변식, 관계는 [`docs/domain`](../../../domain)에서 확인한다.
- Aggregate 경계가 불명확하거나 문서와 구현이 다르면 구현 전에 확인한다.

## 불변식 관리

불변식은 적용 범위에 따라 책임을 나눈다.

### 객체 내부 불변식

하나의 Entity나 Value Object만으로 판단할 수 있는 규칙은 해당 객체가 관리한다.

예를 들어 `InventoryReservation`의 상태 전이나 `Money`의 금액 유효성처럼 객체 자체의 상태와 값에 대한 규칙이다.

### Root Aggregate 불변식

Aggregate 내부 여러 객체의 관계를 판단해야 하는 규칙은 Root Aggregate가 관리한다.

예를 들어 `Product`의 `OptionValue`를 `Variant`가 사용 중이면 삭제할 수 없는 규칙은 `Product`가 판단한다.

외부에서 내부 객체를 조합해 이 규칙을 판단하지 않는다.

### 외부 불변식

여러 Aggregate, 저장소 조회, 다른 모듈의 계약이 필요한 규칙은 Application에서 관리한다.

예를 들어 상품명이 이미 존재하는지 확인하는 규칙처럼 하나의 Product 내부만으로 판단할 수 없는 규칙이다.

Application은 필요한 외부 정보를 확인한 뒤 Domain 객체의 행위를 호출한다. Domain 객체가 Repository나 다른 모듈을 직접 호출하지 않는다.

## Entity

- 식별자를 가지고 생명주기 동안 상태가 변경될 수 있는 객체를 둔다.
- 상태 변경은 단순 Setter가 아니라 의미 있는 행위 메서드로 제공한다.
- 행위 메서드는 객체 또는 Aggregate의 불변식을 검증한 뒤 상태를 변경한다.
- 생성 시점부터 유효한 상태를 갖도록 한다.

```java
public class Inventory {

    private Quantity quantity;

    public void reserve(Quantity requested) {
        if (quantity.isLessThan(requested)) {
            throw new InsufficientInventoryException();
        }
        quantity = quantity.subtract(requested);
    }
}
```

## Value Object

- 식별자보다 값 자체가 중요한 개념을 표현한다.
- 불변 객체로 작성한다.
- 값의 유효성 검증과 값에 대한 연산을 객체 내부에 둔다.
- 금액, 수량, 이메일, 식별자, 상태 등 원시 타입만으로 의미가 부족한 개념에 사용한다.

## Domain 규칙 객체

먼저 해당 규칙에 필요한 정보를 가장 많이 가진 Entity, Value Object, Aggregate에 책임을 둔다. 하나의 객체에 자연스럽게 속하지 않고 여러 도메인 객체의 협력이 필요한 순수한 규칙만 Domain 규칙 객체로 분리할 수 있다.

```text
필요한 정보를 가장 많이 가진 객체 확인
        ↓
Entity / VO / Aggregate에 책임 배치
        ↓
어느 객체에도 자연스럽게 속하지 않을 때만
Domain 규칙 객체로 분리
```

- Application의 유스케이스 흐름을 담당하지 않는다.
- Repository, Controller, 외부 API를 호출하지 않는다.
- `Service` 접미사 대신 도메인 역할을 나타내는 명사형 이름을 사용한다.
- `Policy`, `Validator`, `Calculator`, `Rule` 등을 역할명으로 사용할 수 있다.

비즈니스 규칙의 의미와 적용 범위가 불명확하면 Domain 규칙 객체를 임의로 추가하지 않고 도메인 요구사항을 확인한다.

```text
OrderPricingPolicy
OrderCancellationPolicy
InventoryReservationPolicy
PaymentAmountValidator
DiscountCalculator
```

## 설계 원칙

- 규칙이 적용되는 객체에 규칙을 최대한 가깝게 둔다.
- 같은 불변식과 상태 변경 규칙을 여러 계층에 중복 구현하지 않는다.
- 단순 Getter와 Setter만 제공하는 객체가 되지 않도록 한다.
- Aggregate의 생명주기와 불변식이 불명확하면 구현 전에 확인한다.
## 엔티티 구현 규칙

- 엔티티는 `@Entity`, `@Table`을 사용하고 테이블명을 명시한다.
- 식별자는 `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`를 사용한다.
- JPA 기본 생성자는 `@NoArgsConstructor(access = AccessLevel.PROTECTED)`로 제한한다.
- 속성에는 Lombok `@Getter`를 사용하며 수동 getter 메서드를 작성하지 않는다.
- 생성자는 속성 선언 바로 다음에 배치한다.
- 외부 생성은 정적 팩토리 메서드(`of`, `from`)를 사용하고, 빌더는 private으로 제한한다.
- 생성 시 불변식은 생성자에서 검증한다. 검증 메서드는 `//불변식======================================` 주석 아래에 배치한다.
- 필수 속성의 `@Column`에는 `nullable = false`를 명시한다.
- 상태 변경은 행위 메서드로 제공하며, 단순 setter를 사용하지 않는다.
- 불변식 또는 계약을 확인하는 검증은 재사용 여부와 관계없이 별도 메서드로 추출한다.
- Entity 간 양방향 연관관계에서는 `@OneToMany(mappedBy = "연관 필드명")`와 `@ManyToOne`의 `@JoinColumn`을 사용해 연관관계 주인을 명확히 한다.
- `@OneToMany`에 `mappedBy` 또는 `@JoinColumn` 없이 선언하지 않는다.
- Aggregate Root가 내부 Entity의 생성과 상태 변경을 관리하고, 연관관계의 양쪽 객체를 함께 갱신한다.
- 행위 메서드의 사전조건과 상태 전이 계약은 `ensure...` 메서드로 분리한다.
- 생성자와 행위에 전달된 값 자체의 검증은 `validate...` 메서드로 분리한다.
- `ensure...`와 `validate...` 메서드는 `//불변식======================================` 주석 아래에 배치한다.
- 행위 메서드 본문에는 검증 조건을 직접 작성하지 않고 검증 메서드 호출과 상태 변경 흐름만 드러낸다.
- 행위 메서드에는 도메인 상태 변경 흐름이 드러나도록 하고, 조건 검증의 세부 구현은 숨긴다.
