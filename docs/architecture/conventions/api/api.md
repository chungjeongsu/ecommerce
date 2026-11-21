# API 컨벤션

## 목적

`api` 패키지는 모듈이 다른 모듈에 제공하는 정보와 행위에 대한 공개 계약을 정의한다.

API 사용자는 모듈 내부의 Entity, Repository, 영속성 모델, 구현 방식을 알지 않아도 계약만으로 기능을 사용할 수 있어야 한다.

## 구조

```text
{module}/api/
├─ {ActionName}Api.java
├─ request/
│  └─ ...Req.java
└─ response/
   └─ ...Res.java
```

- API 인터페이스는 `api` 패키지에 둔다.
- API 입력 모델은 `request` 패키지에 둔다.
- API 반환 모델은 `response` 패키지에 둔다.
- Request와 Response는 모듈 간 통신을 위한 계약 모델이다.
- 구체적인 클래스명과 DTO 명명 규칙은 이 프로젝트의 명명 컨벤션을 따른다.

## API의 책임

API는 모듈이 외부에 제공하는 유스케이스 단위의 계약이다.

하나의 API 계약은 다음 내용을 명확히 표현해야 한다.

- 어떤 행위를 요청하는가
- 어떤 입력을 전달하는가
- 어떤 결과를 반환하는가
- 어떤 실패 또는 예외가 발생할 수 있는가
- 입력이 만족해야 하는 조건은 무엇인가
- 실행 후 어떤 상태를 보장하는가

API는 모듈 내부의 조회 방법이나 상태 변경 절차를 노출하지 않는다.

```java
public interface InventoryApi {

    ReservationRes reserve(ReserveReq request);
}
```

위 계약을 사용하는 모듈은 재고의 Entity 구조, Repository 조회 방식, 예약 저장 방식을 알 필요가 없다.

## 구현 위치

API 구현체는 공개 모듈 영역이 아닌 해당 모듈의 `internal/application`에 둔다.

```text
{module}/
├─ api/
│  ├─ {ActionName}Api.java
│  ├─ request/
│  └─ response/
└─ internal/
   └─ application/
      └─ API 구현체
```

API 구현체는 API 요청을 애플리케이션 입력으로 변환하고, 필요한 Usecase를 호출한 뒤 결과를 API Response로 변환한다.

```text
API Request
    ↓
Application Command 또는 Usecase 입력
    ↓
Usecase
    ↓
Application Result
    ↓
API Response
```

## 모델 경계

- API Request/Response는 공개 계약 모델이다.
- Application Command/Result와 필요에 따라 분리한다.
- 다른 모듈의 Entity, Value Object, Repository를 API 모델로 직접 노출하지 않는다.
- 모듈 내부의 Request/Response 또는 외부 시스템의 Request/Response를 API 모델로 재사용하지 않는다.
- 계약에 불필요한 내부 상태와 영속성 정보를 포함하지 않는다.

## 계약 조건

API에 정의된 입력은 호출자가 만족해야 하는 조건을 명확히 해야 한다.

예를 들어 재고 예약 API라면 다음과 같은 조건을 계약에 포함할 수 있다.

- 상품 식별자와 수량은 필수다.
- 수량은 1 이상이다.
- 예약 가능한 재고가 있어야 한다.
- 예약 성공 시 예약 식별자를 반환한다.
- 예약할 수 없는 경우 정해진 실패 결과 또는 예외를 반환한다.

계약 조건은 API 계약의 일부로 취급한다.

API는 모듈의 공개된 행위와 필요한 정보만 제공하며, 내부 구현의 편의를 위한 메서드를 추가하지 않는다.
