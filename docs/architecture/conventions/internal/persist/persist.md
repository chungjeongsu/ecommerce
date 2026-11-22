# Persist 코드 컨벤션

## 책임

`internal/persist`는 데이터의 저장과 조회를 담당한다.

- Repository 인터페이스와 구현을 둔다.
- JPA Entity와 영속성 모델을 관리한다.
- JPQL 또는 JDBC를 사용해 조회를 구현한다.
- 영속성 모델과 Domain 객체 사이의 변환을 담당한다.

조회 방식, SQL, JPA 설정 등 영속성 기술의 세부사항은 Persist 밖으로 노출하지 않는다.

## 구조

```text
{module}/internal/persist/
├─ {Name}Entity.java
├─ {Name}Repository.java           # Repository 인터페이스
├─ {Name}RepositoryCustom.java     # 직접 구현이 필요한 경우
├─ {Name}RepositoryImpl.java       # Custom 구현체
└─ ...
```

실제 책임이 필요할 때만 Entity, Repository, Mapper 등의 클래스를 생성한다.

## Repository

Repository 인터페이스와 구현체는 모두 `persist`에 둔다.

Spring Data JPA가 제공하는 기본 CRUD와 조회 메서드로 충분한 경우 `JpaRepository`를 확장한다.

```java
public interface InventoryRepository
        extends JpaRepository<InventoryEntity, Long> {

    Optional<InventoryEntity> findByVariantId(Long variantId);
}
```

- Repository 이름은 대상 영속성 모델의 역할을 나타낸다.
- Spring Data 메서드 이름으로 표현할 수 있는 단순 조회는 메서드 쿼리를 사용한다.
- Application과 Domain에는 Spring Data Repository 타입을 노출하지 않는다.
- Repository의 반환 모델은 Persist의 영속성 모델을 기준으로 한다.

## JPQL

메서드 이름으로 표현하기 어렵거나 명시적인 조회 조건이 필요한 경우 JPQL을 사용한다.

```java
@Query("""
    select inventory
    from InventoryEntity inventory
    where inventory.variantId = :variantId
    """)
Optional<InventoryEntity> findByVariantId(@Param("variantId") Long variantId);
```

- JPQL은 Entity와 객체 관계를 기준으로 작성한다.
- 복잡한 조회 조건은 쿼리의 의도가 드러나는 Repository 메서드로 감싼다.
- 조회 목적에 불필요한 Entity 전체 로딩을 피한다.
- JPQL에 영속성 외 계층의 업무 규칙을 넣지 않는다.

## JDBC

JPA보다 JDBC가 적절한 경우 JDBC 기반 구현을 사용한다.

- 대량 조회나 대량 변경이 필요한 경우
- 복잡한 SQL 또는 데이터베이스 기능이 필요한 경우
- JPA Entity 매핑보다 직접적인 SQL 처리가 적절한 경우

JDBC 구현에서도 Repository가 외부에 제공하는 책임은 동일하게 유지한다. JDBC 사용 여부가 Application이나 Domain으로 노출되지 않도록 한다.

## 직접 구현

Spring Data JPA의 기본 기능이나 JPQL만으로 구현하기 어려운 경우 Repository 인터페이스와 구현체를 분리한다.

```java
public interface InventoryRepository
        extends JpaRepository<InventoryEntity, Long>, InventoryRepositoryCustom {
}

public interface InventoryRepositoryCustom {

    List<InventoryEntity> findReservable(...);
}
```

```java
class InventoryRepositoryImpl implements InventoryRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<InventoryEntity> findReservable(...) {
        // JDBC 기반 구현
    }
}
```

- 인터페이스는 Repository가 제공하는 저장과 조회의 책임을 정의한다.
- 구현체는 JDBC, JPQL, Querydsl 등 필요한 영속성 기술을 선택한다.
- 구현체의 기술적 세부사항은 인터페이스 밖으로 노출하지 않는다.
- Spring Data Repository와 직접 구현 Repository를 목적에 따라 분리한다.

## Entity와 Domain 변환

- JPA Entity는 Persist 내부에 둔다.
- JPA Entity를 Domain 객체나 API 응답으로 직접 노출하지 않는다.
- Domain 객체와 영속성 모델이 다르면 변환 책임을 Persist에 둔다.
- 영속성 모델의 구조가 Domain의 불변식과 행위를 대신하지 않도록 한다.

## 변경을 위한 Aggregate 조회

Aggregate를 변경하는 유스케이스는 Aggregate Root를 기준으로 조회한다.

- 변경에 필요한 하위 객체는 Aggregate Root와 함께 조회한다.
- 연관 객체를 지연 로딩에 의존해 순차적으로 조회하지 않는다.
- 필요한 조회 경로에는 JPQL `fetch join` 등 적절한 조회 전략을 사용해 N+1을 방지한다.
- 조회 메서드는 특정 변경 유스케이스에 필요한 Aggregate 구조를 명확히 표현한다.
- Aggregate 경계를 넘어 다른 Aggregate를 한 번에 조회하거나 변경하지 않는다.

```java
@Query("""
    select distinct product
    from ProductEntity product
    join fetch product.options options
    join fetch options.values
    where product.id = :productId
    """)
Optional<ProductEntity> findForChange(@Param("productId") Long productId);
```

위 예시는 변경에 필요한 하위 객체가 반드시 존재하는 경우의 `inner join fetch` 조회다. 하위 객체가 없어도 Aggregate Root를 조회해야 하는 경우에만 `left join fetch`를 사용한다.

`fetch join`은 필요한 관계에만 적용한다. 여러 컬렉션을 동시에 조인해 결과가 폭증하거나 조회 목적을 넘어선 데이터를 로딩하지 않도록 한다.

## 계층 경계

- Controller는 Repository를 직접 호출하지 않는다.
- Domain은 Repository 구현체, JPA, JDBC를 알지 않는다.
- Application은 Persist가 제공하는 Repository를 유스케이스 흐름 안에서 사용한다.
- 다른 모듈은 해당 모듈의 Persist에 직접 의존하지 않는다.
- Persist 작업은 Entity 매핑, Repository 저장·조회, Flyway 스키마 일치 여부를 함께 확인한다.
- Repository 변경 시 실제 테스트 데이터베이스를 사용하는 통합 테스트를 작성한다.
- Aggregate를 수정하기 위한 조회는 Root와 필요한 내부 객체를 함께 로딩하는지 확인한다.
- JPA 연관관계는 `mappedBy` 또는 `@JoinColumn`으로 연관관계 주인을 명확히 한다.
