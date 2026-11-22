# 0. ecommerce 개인 프로젝트의 목적

도메인 중심 이커머스 시스템 개발을 통해 요구사항 구현에서 나타날 수 있는 <ins>***문제 해결 역량을 강화***</ins>하고, AI Agent 기반 프롬프트를 구축하여 <ins>***스스로의 설계·구현·검증 과정을 체계화***</ins>하기 위한 프로젝트.

# 1. 기술 스택

주 기술 스택은 애플리케이션의 핵심 구조와 구현에 사용하는 기술, 

보조 기술 스택은 개발·테스트·문서화·자동화를 지원하는 기술로 구분했습니다.

## 1-1. 주 기술 스택

| 주 기술 스택 | 설명 |
|---|---|
| <img src="https://cdn.simpleicons.org/openjdk" width="18" height="18"> Java 21 | 백엔드 애플리케이션 개발 |
| <img src="https://cdn.simpleicons.org/springboot" width="18" height="18"> Spring Boot 4.1.2 | 애플리케이션 프레임워크 |
| <img src="https://cdn.simpleicons.org/spring" width="18" height="18"> Spring Modulith | 도메인별 모듈 경계 및 의존성 관리 |
| <img src="https://cdn.simpleicons.org/hibernate" width="18" height="18"> Spring Data JPA | ORM 기반 영속성 관리 |
| <img src="https://cdn.simpleicons.org/mysql" width="18" height="18"> MySQL | 관계형 데이터베이스 |
| <img src="https://cdn.simpleicons.org/gradle" width="18" height="18"> Gradle | 빌드 및 의존성 관리 |

## 1-2. 보조 기술 스택

| 보조 기술 스택 | 설명 |
|---|---|
| <img src="https://cdn.simpleicons.org/flyway" width="18" height="18"> Flyway | 데이터베이스 스키마 및 마이그레이션 관리 |
| <img src="https://cdn.simpleicons.org/spring" width="18" height="18"> Spring REST Docs | 테스트 기반 REST API 문서화 |
| <img src="https://cdn.simpleicons.org/junit5" width="18" height="18"> JUnit 5 | 단위·통합 테스트 |
| <img src="https://cdn.simpleicons.org/openai" width="18" height="18"> Codex | AI Agent 기반 개발 자동화 및 TDD 워크플로 수행 |
| <img src="https://cdn.simpleicons.org/githubactions" width="18" height="18"> GitHub Actions | CI/CD 자동화 |

# 2. 도메인

| 도메인 | 주요 객체 | 책임 |
|---|---|---|
| **Product** | Product | 상품 기본 정보, 판매 상태, 옵션과 Variant 생명주기 관리 |
| | Option | 상품 옵션 이름과 옵션 값 관리 |
| | OptionValue | 옵션 값 이름 관리 및 사용 여부 판단 |
| | Variant | 옵션 조합별 가격과 판매 상태 관리 |
| **Inventory** | Inventory | Variant별 총 재고, 가용 재고, 예약 재고 관리 |
| | InventoryReservation | 주문별 재고 예약과 확정·해제 상태 관리 |
| **Order** | Order | 구매자, 주문 항목, 주문 금액, 주문 상태와 만료 관리 |
| | OrderItem | 주문 수량, 단가, 항목 금액 관리 |
| | ProductSnapshot | 주문 당시 상품명, 옵션, 가격 보존 |
| **Payment** | Payment | 주문 금액, PG 식별자, 멱등키와 결제 상태 관리 |
| **User** | User | 사용자 정보, 역할, 인증 정보와 탈퇴 상태 관리 |

# 3. 규칙(불변식)

규칙은 아래와 같은 구조로 설계하였습니다.

## 3-1. 규칙(불변식) 설계 기준1 - 규칙 종류 구분

| 규칙(불변식) 종류 | 설명 |
|---|---|
| 객체 불변식 | 객체 내부 속성이 만족해야하는 규칙 |
| 도메인 불변식 | Root Aggregate로 변경/관리되는 모든 도메인 내 객체들이 만족해야 하는 규칙 |
| 외부 불변식 | 도메인 간 협력에서 만족해야하는 규칙 유즈케이스라고도 부름 |
