package com.ecommerce.project.inventory.internal.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "inventory")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_status", nullable = false)
    private InventoryStatus status;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryReservation> reservations = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Inventory(final Long variantId, final int totalQuantity) {
        this.variantId = validateVariantId(variantId);
        this.totalQuantity = validateTotalQuantity(totalQuantity);
        this.status = InventoryStatus.ACTIVE;
    }

    public InventoryReservation reserve(final Long orderId, final Long orderItemId, final int quantity) {
        ensureActive();
        ensureReservationDoesNotExist(orderId, orderItemId);
        ensureReservableQuantity(quantity);

        InventoryReservation reservation = InventoryReservation.of(this, orderId, orderItemId, quantity);
        reservations.add(reservation);
        return reservation;
    }

    public int availableQuantity() {
        return totalQuantity - reservedQuantity();
    }

    public void confirmReservation(final InventoryReservation reservation) {
        ensureReservationBelongsToInventory(reservation);
        reservation.confirm();
        totalQuantity -= reservation.getQuantity();
    }

    public void releaseReservation(final InventoryReservation reservation) {
        ensureReservationBelongsToInventory(reservation);
        reservation.release();
    }

    public void close() {
        ensureClosing();
        ensureAllReservationsFinished();
        status = InventoryStatus.CLOSED;
    }

    public void startClosing() {
        ensureActive();
        status = InventoryStatus.CLOSING;
    }

    public void resume() {
        ensureClosed();
        status = InventoryStatus.ACTIVE;
    }

    public static Inventory of(final Long variantId, final int totalQuantity) {
        return Inventory.builder()
                .variantId(variantId)
                .totalQuantity(totalQuantity)
                .build();
    }

    private int reservedQuantity() {
        return reservations.stream()
                .filter(reservation -> reservation.getStatus() == InventoryReservationStatus.RESERVED)
                .mapToInt(InventoryReservation::getQuantity)
                .sum();
    }

    private boolean hasReservation(final Long orderId, final Long orderItemId) {
        return reservations.stream().anyMatch(reservation ->
                reservation.getOrderId().equals(orderId)
                        && reservation.getOrderItemId().equals(orderItemId)
        );
    }

    //불변식======================================

    private void ensureActive() {
        if (status != InventoryStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태의 재고만 수행할 수 있습니다.");
        }
    }

    private void ensureClosing() {
        if (status != InventoryStatus.CLOSING) {
            throw new IllegalStateException("마감 중인 재고만 종료할 수 있습니다.");
        }
    }

    private void ensureClosed() {
        if (status != InventoryStatus.CLOSED) {
            throw new IllegalStateException("종료된 재고만 재개할 수 있습니다.");
        }
    }

    private void ensureReservationDoesNotExist(final Long orderId, final Long orderItemId) {
        if (hasReservation(orderId, orderItemId)) {
            throw new IllegalStateException("동일한 주문의 재고는 중복 예약할 수 없습니다.");
        }
    }

    private void ensureReservableQuantity(final int quantity) {
        int reservableQuantity = reservedQuantity() + validateQuantity(quantity);
        if (reservableQuantity > totalQuantity) {
            throw new IllegalStateException("예약 수량이 총 재고 수량을 초과할 수 없습니다.");
        }
    }

    private void ensureReservationBelongsToInventory(final InventoryReservation reservation) {
        if (!reservations.contains(reservation)) {
            throw new IllegalArgumentException("해당 재고에 속한 예약만 처리할 수 있습니다.");
        }
    }

    private int validateTotalQuantity(final int totalQuantity) {
        if (totalQuantity < 0) {
            throw new IllegalArgumentException("총 재고 수량은 0 이상이어야 합니다.");
        }
        return totalQuantity;
    }

    private Long validateVariantId(final Long variantId) {
        if (variantId == null) {
            throw new IllegalArgumentException("Variant ID는 필수입니다.");
        }
        return variantId;
    }

    private int validateQuantity(final int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("예약 수량은 1 이상이어야 합니다.");
        }
        return quantity;
    }

    private void ensureAllReservationsFinished() {
        if (reservations.stream().anyMatch(reservation -> reservation.getStatus() == InventoryReservationStatus.RESERVED)) {
            throw new IllegalStateException("예약이 종료되지 않은 재고는 종료할 수 없습니다.");
        }
    }
}
