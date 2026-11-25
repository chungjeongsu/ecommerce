---
name: exception-convention
description: 공통 BusinessException, 도메인별 ErrorCode와 API 예외 응답 처리 규칙을 정의한다.
---

# Exception 코드 컨벤션

공통 예외 처리 구조와 코드 컨벤션을 정의한다. 로그 메시지는 한국어로 예외 맥락을 파악할 수 있도록 작성한다.

## 패키지 규칙

기본 패키지는 `com.ecommerce.project`이다.

- 공통 예외: `global.exception`
- 주문 예외 코드: `order.internal.exception`
- 결제 예외 코드: `payment.internal.exception`
- 상품 예외 코드: `product.internal.exception`

## ErrorCode

모든 비즈니스 에러 코드는 다음 인터페이스를 구현한다.

```java
public interface ErrorCode {
    HttpStatus getStatus();
    String getMessage();
}
```

에러 코드는 도메인별 enum으로 정의한다.

```java
@Getter
@RequiredArgsConstructor
public enum ProductException implements ErrorCode {
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "재고가 부족합니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

- 도메인별 에러 코드는 `OrderException`, `PaymentException`, `ProductException`처럼 분리한다.
- enum 상수명은 대문자 스네이크 케이스를 사용한다.
- HTTP 상태 코드와 응답 메시지는 에러 코드 enum이 관리한다.
- 서비스 코드에 비즈니스 에러 메시지를 직접 하드코딩하지 않는다.
- Lombok의 `@Getter`, `@RequiredArgsConstructor`를 기존 스타일에 맞게 사용한다.

## BusinessException

비즈니스 로직에서 예외를 발생시킬 때는 공통 `BusinessException`을 사용한다.

```java
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

사용 예시:

```java
throw new BusinessException(PaymentException.PAYMENT_NOT_FOUND);
```

규칙:

- 존재하지 않는 리소스, 상태 불일치, 재처리 불가, 재고 부족 등은 `BusinessException`을 사용한다.
- `RuntimeException`을 직접 상속한 도메인별 예외 클래스를 새로 만들지 않는다.
- 기존의 `PaymentNotFoundException`과 같은 개별 예외 클래스는 신규 코드에서 사용하지 않는다.
- 새로운 비즈니스 오류가 필요하면 먼저 도메인의 ErrorCode enum에 항목을 추가한다.
- 예외 메시지는 ControllerAdvice가 ErrorCode를 통해 응답하도록 한다.

## ControllerAdvice

공통 API 예외 처리는 `global.exception.ControllerAdvice`에서 담당한다.

```java
@Slf4j(topic = "API_LOGGER")
@RestControllerAdvice
public class ControllerAdvice {
    // exception handlers
}
```

처리 우선순위는 다음과 같다.

1. `MethodArgumentNotValidException`
2. `BusinessException`
3. 기타 `Exception`

비즈니스 예외 처리 규칙:

- HTTP 상태 코드는 `errorCode.getStatus()`를 사용한다.
- 응답 메시지는 `errorCode.getMessage()`를 사용한다.
- 로그에는 error code와 message를 남긴다.

검증 예외 처리 규칙:

- `MethodArgumentNotValidException`의 첫 번째 field error를 사용한다.
- 메시지는 `fieldName + " " + defaultMessage` 형식으로 만든다.
- HTTP 400을 반환한다.

예상하지 못한 예외 처리 규칙:

- HTTP 500을 반환한다.
- 외부 응답에는 일반화된 메시지인 `예상하지 못한 서버 오류가 발생했습니다.`만 반환한다.
- 상세 stack trace는 서버 로그에만 기록한다.
- 내부 구현 정보와 민감 정보는 API 응답에 노출하지 않는다.

## ErrorResponse

API 예외 응답은 record DTO를 사용한다.

```java
public record ErrorResponse(
        String message
) {
}
```

응답 형식은 기존의 `ErrorResponse(String message)`를 유지한다.

## 도메인 내부 검증

도메인 객체 자체의 불변식 위반은 `IllegalArgumentException`을 사용한다.

```java
if (price < 0) {
    throw new IllegalArgumentException("price는 음수일 수 없습니다.");
}
```

- null, 빈 문자열, 음수, 허용되지 않는 값 등 객체 자체의 불변식 검증은 도메인 객체 내부에 둔다.
- 도메인 상태상 비즈니스 오류이며 API 응답으로 변환되어야 하는 경우에는 `BusinessException`을 사용한다.
- 설정 또는 시스템 초기화 오류는 `IllegalStateException`을 사용할 수 있다.
- 예외 메시지는 상황을 설명하되 민감한 정보를 포함하지 않는다.

## 구현 시 체크리스트

- 기존 `BusinessException`, `ErrorCode`, `ControllerAdvice`를 먼저 확인하고 재사용한다.
- 새 예외 클래스를 만들기 전에 기존 도메인 ErrorCode enum에 추가할 수 있는지 검토한다.
- 비즈니스 에러 메시지를 서비스 코드에 직접 작성하지 않는다.
- 에러 코드와 HTTP 상태 코드의 매핑을 명확히 한다.
- API 응답 형식 `ErrorResponse(String message)`를 변경하지 않는다.
- 예상하지 못한 예외의 원인을 외부에 노출하지 않는다.
- 도메인 불변식 위반과 비즈니스 오류를 구분한다.
- 새 코드는 기존 모듈별 `internal.exception` 패키지 구조와 네이밍을 유지한다.
