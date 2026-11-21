# Spring Modulith 컨벤션

## 목적

Spring Modulith를 사용해 단일 애플리케이션 안에서 도메인 모듈의 경계와 의존성을 관리한다.

모듈 간 도메인 의존성 자체를 제거하지 않는다. 다른 모듈의 내부 구현이 아니라 공개된 계약을 통해 통신하도록 하여 변경 전파와 결합도를 줄인다.

## 모듈 인식

Spring Modulith는 애플리케이션 루트 패키지 바로 아래의 패키지를 모듈로 인식한다.

```text
com.ecommerce.project/
├─ ProjectApplication.java
├─ product/
├─ inventory/
├─ order/
├─ payment/
└─ user/
```

각 도메인 패키지는 독립적인 모듈로 취급한다. Settlement는 구현을 시작할 때 동일한 기준을 적용한다.

## 모듈 공개 영역과 내부 영역

모듈은 외부에 공개하는 `api`와 모듈 내부 구현인 `internal`로 구분한다.

```text
{module}/
├─ api/                         # 모듈 외부 공개 영역
│  └─ 공개 API 계약
└─ internal/                    # 모듈 내부 영역
   ├─ application/
   ├─ domain/
   ├─ persist/
   ├─ infra/
   └─ presentation/
```

- 다른 모듈은 `api`만 사용한다.
- 다른 모듈은 `internal`의 클래스, DTO, Entity, Repository를 직접 참조하지 않는다.
- 모듈 외부에 공개할 필요가 없는 타입은 `internal`에 둔다.

## 모듈 간 의존성 규칙

1. 모듈 간 호출은 상대 모듈의 `api`를 통해서만 수행한다.
2. 다른 모듈의 `internal` 패키지에 직접 의존하지 않는다.
3. 다른 모듈의 Entity, Repository, 영속성 모델을 직접 사용하지 않는다.
4. 모듈의 내부 구현 변경이 호출 모듈로 전파되지 않도록 공개 계약으로 필요한 정보와 행위만 제공한다.
5. 모듈 간 의존성이 순환하지 않도록 한다.
6. 여러 모듈이 복잡하게 직접 호출해야 하는 경우 공개 API의 책임과 모듈 경계를 재검토한다.

메시지 또는 이벤트 기반 협력 규칙은 공개 이벤트 설계 시 별도로 정의한다.

## 공개 영역 설정

- 모듈 경계와 공개 영역은 패키지 구조 및 Spring Modulith 설정으로 명시한다. 
- `api`를 이름 있는 인터페이스로 관리할 경우 `@NamedInterface`를 사용한다.
- api 패키지 루트와 하위에`package-info.java`로 Modulith에 해당 패키지의 공개 여부를 정의한다.

```java
@ApplicationModule
package com.ecommerce.project.inventory;

import org.springframework.modulith.ApplicationModule;
```

```java
@NamedInterface("api")
package com.ecommerce.project.inventory.api;

import org.springframework.modulith.NamedInterface; 
```

`api`의 하위 패키지를 공개 계약으로 사용하는 경우 해당 패키지의 공개 범위도 명시적으로 관리한다.

