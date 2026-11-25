package com.ecommerce.project.order.internal.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "orders",
        uniqueConstraints = @UniqueConstraint(name = "uk_orders_order_code", columnNames = "order_code")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(name = "order_code", nullable = false, columnDefinition = "BINARY(16)")
    private UUID orderCode;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    @Getter(AccessLevel.NONE)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    private Order(
            final UUID orderCode,
            final Long buyerId,
            final List<OrderItem> items,
            final LocalDateTime createdAt,
            final LocalDateTime expiresAt
    ) {
        this.orderCode = validateOrderCode(orderCode);
        this.buyerId = validateBuyerId(buyerId);
        this.createdAt = validateCreatedAt(createdAt);
        this.expiresAt = validateExpiresAt(this.createdAt, expiresAt);
        this.items = validateItems(items);
        this.status = OrderStatus.READY;
    }

    public void pay() {
        ensureReady();
        status = OrderStatus.PAID;
    }

    public void cancel() {
        ensureReady();
        status = OrderStatus.CANCELLED;
    }

    public void expire(final LocalDateTime currentTime) {
        ensureReady();
        ensureExpirationReached(currentTime);
        status = OrderStatus.EXPIRED;
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    public long getTotalAmount() {
        long totalAmount = 0L;
        for (OrderItem item : items) {
            totalAmount = Math.addExact(totalAmount, item.getAmount());
        }
        return totalAmount;
    }

    public static Order of(
            final UUID orderCode,
            final Long buyerId,
            final List<OrderItem> items,
            final LocalDateTime createdAt,
            final LocalDateTime expiresAt
    ) {
        return new Order(orderCode, buyerId, items, createdAt, expiresAt);
    }

    // 불변식 =====================================

    private UUID validateOrderCode(final UUID orderCode) {
        if (orderCode == null) {
            throw new IllegalArgumentException("주문번호는 필수입니다.");
        }
        return orderCode;
    }

    private Long validateBuyerId(final Long buyerId) {
        if (buyerId == null) {
            throw new IllegalArgumentException("구매자 ID는 필수입니다.");
        }
        return buyerId;
    }

    private LocalDateTime validateCreatedAt(final LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("주문 생성 시각은 필수입니다.");
        }
        return createdAt;
    }

    private LocalDateTime validateExpiresAt(
            final LocalDateTime createdAt,
            final LocalDateTime expiresAt
    ) {
        if (expiresAt == null || !expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("주문 만료 시각은 생성 시각보다 이후여야 합니다.");
        }
        return expiresAt;
    }

    private List<OrderItem> validateItems(final List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("주문에는 하나 이상의 주문 항목이 필요합니다.");
        }
        if (items.stream().anyMatch(item -> item == null)) {
            throw new IllegalArgumentException("주문 항목은 null일 수 없습니다.");
        }

        Set<Long> variantIds = new HashSet<>();
        long totalAmount = 0L;
        for (OrderItem item : items) {
            if (!variantIds.add(item.getVariantId())) {
                throw new IllegalArgumentException("같은 주문에 동일한 Variant를 중복해서 포함할 수 없습니다.");
            }
            try {
                totalAmount = Math.addExact(totalAmount, item.getAmount());
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException("주문 총액이 허용 범위를 초과했습니다.", exception);
            }
        }
        return new ArrayList<>(items);
    }

    private void ensureReady() {
        if (status != OrderStatus.READY) {
            throw new IllegalStateException("준비 상태의 주문만 상태를 변경할 수 있습니다.");
        }
    }

    private void ensureExpirationReached(final LocalDateTime currentTime) {
        if (currentTime == null) {
            throw new IllegalArgumentException("현재 시각은 필수입니다.");
        }
        if (currentTime.isBefore(expiresAt)) {
            throw new IllegalStateException("결제 기한 전에는 주문을 만료할 수 없습니다.");
        }
    }
}
