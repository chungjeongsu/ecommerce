package com.ecommerce.project.inventory.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "inventory_reservation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_reservation_id")
    private Long id;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_reservation_status", nullable = false)
    private InventoryReservationStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    private InventoryReservation(
            final Inventory inventory,
            final Long orderId,
            final Long orderItemId,
            final int quantity
    ) {
        this.inventory = validateInventory(inventory);
        this.orderId = validateOrderId(orderId);
        this.orderItemId = validateOrderItemId(orderItemId);
        this.quantity = validateQuantity(quantity);
        this.status = InventoryReservationStatus.RESERVED;
    }

    void confirm() {
        ensureReserved();
        status = InventoryReservationStatus.CONFIRMED;
    }

    void release() {
        ensureReserved();
        status = InventoryReservationStatus.RELEASED;
    }

    static InventoryReservation of(
            final Inventory inventory,
            final Long orderId,
            final Long orderItemId,
            final int quantity
    ) {
        return InventoryReservation.builder()
                .inventory(inventory)
                .orderId(orderId)
                .orderItemId(orderItemId)
                .quantity(quantity)
                .build();
    }

    //불변식======================================

    private int validateQuantity(final int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("예약 수량은 1 이상이어야 합니다.");
        }
        return quantity;
    }

    private Long validateOrderId(final Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("주문 ID는 필수입니다.");
        }
        return orderId;
    }

    private Long validateOrderItemId(final Long orderItemId) {
        if (orderItemId == null) {
            throw new IllegalArgumentException("주문상품 ID는 필수입니다.");
        }
        return orderItemId;
    }

    private Inventory validateInventory(final Inventory inventory) {
        if (inventory == null) {
            throw new IllegalArgumentException("재고 예약에는 Inventory가 필요합니다.");
        }
        return inventory;
    }

    private void ensureReserved() {
        if (status != InventoryReservationStatus.RESERVED) {
            throw new IllegalStateException("종료된 재고 예약의 상태는 변경할 수 없습니다.");
        }
    }
}
