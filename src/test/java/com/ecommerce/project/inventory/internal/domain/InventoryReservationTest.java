package com.ecommerce.project.inventory.internal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

class InventoryReservationTest {

    @Test
    @DisplayName("재고 예약은 생성 시 예약 상태여야 한다")
    void reservation_starts_with_reserved_status() {
        // given
        InventoryReservation reservation = InventoryReservation.of(Inventory.of(1L, 10), 3L, 4L, 5);

        // when
        InventoryReservationStatus status = reservation.getStatus();

        // then
        assertEquals(InventoryReservationStatus.RESERVED, status);
    }

    @Test
    @DisplayName("재고 예약 수량은 1 이상이어야 한다")
    void reservation_quantity_must_be_positive() {
        // given
        int invalidQuantity = 0;

        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> InventoryReservation.of(Inventory.of(1L, 10), 3L, 4L, invalidQuantity)
        );
    }

    @Test
    @DisplayName("재고 예약에는 Inventory가 필요하다")
    void inventory_is_required_for_reservation() {
        // given

        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> InventoryReservation.of(null, 3L, 4L, 5)
        );
    }

    @Test
    @DisplayName("재고 예약에는 주문 ID가 필요하다")
    void order_id_is_required_for_reservation() {
        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> InventoryReservation.of(Inventory.of(1L, 10), null, 4L, 5)
        );
    }

    @Test
    @DisplayName("재고 예약에는 주문상품 ID가 필요하다")
    void order_item_id_is_required_for_reservation() {
        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> InventoryReservation.of(Inventory.of(1L, 10), 3L, null, 5)
        );
    }

    @Test
    @DisplayName("예약 상태의 재고 예약은 확정할 수 있어야 한다")
    void reserved_reservation_can_be_confirmed() {
        // given
        InventoryReservation reservation = InventoryReservation.of(Inventory.of(1L, 10), 3L, 4L, 5);

        // when
        reservation.confirm();

        // then
        assertEquals(InventoryReservationStatus.CONFIRMED, reservation.getStatus());
    }

    @Test
    @DisplayName("예약 상태의 재고 예약은 해제할 수 있어야 한다")
    void reserved_reservation_can_be_released() {
        // given
        InventoryReservation reservation = InventoryReservation.of(Inventory.of(1L, 10), 3L, 4L, 5);

        // when
        reservation.release();

        // then
        assertEquals(InventoryReservationStatus.RELEASED, reservation.getStatus());
    }

    @Test
    @DisplayName("확정된 재고 예약은 다시 확정하거나 해제할 수 없어야 한다")
    void confirmed_reservation_cannot_be_confirmed_or_released_again() {
        // given
        InventoryReservation reservation = InventoryReservation.of(Inventory.of(1L, 10), 3L, 4L, 5);
        reservation.confirm();

        // when & then
        assertThrows(IllegalStateException.class, reservation::confirm);
        assertThrows(IllegalStateException.class, reservation::release);
    }

    @Test
    @DisplayName("해제된 재고 예약은 다시 확정하거나 해제할 수 없어야 한다")
    void released_reservation_cannot_be_confirmed_or_released_again() {
        // given
        InventoryReservation reservation = InventoryReservation.of(Inventory.of(1L, 10), 3L, 4L, 5);
        reservation.release();

        // when & then
        assertThrows(IllegalStateException.class, reservation::confirm);
        assertThrows(IllegalStateException.class, reservation::release);
    }

    @Test
    @DisplayName("재고 예약의 식별자와 수량은 변경할 수 없어야 한다")
    void reservation_identifiers_and_quantity_are_immutable() {
        // given
        InventoryReservation reservation = InventoryReservation.of(Inventory.of(1L, 10), 3L, 4L, 5);

        // when
        Long id = reservation.getId();
        Inventory inventory = reservation.getInventory();
        Long orderId = reservation.getOrderId();
        Long orderItemId = reservation.getOrderItemId();
        int quantity = reservation.getQuantity();

        // then
        assertEquals(null, id);
        assertEquals(1L, inventory.getVariantId());
        assertEquals(3L, orderId);
        assertEquals(4L, orderItemId);
        assertEquals(5, quantity);
    }

}
