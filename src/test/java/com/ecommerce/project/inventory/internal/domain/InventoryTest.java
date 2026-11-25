package com.ecommerce.project.inventory.internal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InventoryTest {

    @Test
    @DisplayName("재고는 생성 시 활성 상태여야 한다")
    void inventory_starts_active() {
        // given
        Inventory inventory = Inventory.of(1L, 10);

        // when
        InventoryStatus status = inventory.getStatus();

        // then
        assertEquals(InventoryStatus.ACTIVE, status);
    }

    @Test
    @DisplayName("총 재고 수량은 0 이상이어야 한다")
    void total_quantity_must_not_be_negative() {
        // given
        int invalidQuantity = -1;

        // when & then
        assertThrows(IllegalArgumentException.class, () -> Inventory.of(1L, invalidQuantity));
    }

    @Test
    @DisplayName("재고에는 Variant ID가 필요하다")
    void variant_id_is_required() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> Inventory.of(null, 10));
    }

    @Test
    @DisplayName("활성 재고는 예약을 생성할 수 있어야 한다")
    void active_inventory_can_reserve() {
        // given
        Inventory inventory = Inventory.of(1L, 10);

        // when
        InventoryReservation reservation = inventory.reserve(10L, 20L, 3);

        // then
        assertEquals(InventoryReservationStatus.RESERVED, reservation.getStatus());
        assertEquals(1, inventory.getReservations().size());
    }

    @Test
    @DisplayName("예약 수량이 총 재고를 초과하면 예약할 수 없어야 한다")
    void reservation_quantity_cannot_exceed_total_quantity() {
        // given
        Inventory inventory = Inventory.of(1L, 5);

        // when & then
        assertThrows(IllegalStateException.class, () -> inventory.reserve(10L, 20L, 6));
    }

    @Test
    @DisplayName("예약 수량의 합계가 총 재고를 초과하면 예약할 수 없어야 한다")
    void cumulative_reservation_quantity_cannot_exceed_total_quantity() {
        // given
        Inventory inventory = Inventory.of(1L, 10);
        inventory.reserve(10L, 20L, 6);

        // when & then
        assertThrows(IllegalStateException.class, () -> inventory.reserve(11L, 21L, 5));
    }

    @Test
    @DisplayName("동일한 주문과 주문상품은 중복 예약할 수 없어야 한다")
    void same_order_item_cannot_be_reserved_twice() {
        // given
        Inventory inventory = Inventory.of(1L, 10);
        inventory.reserve(10L, 20L, 1);

        // when & then
        assertThrows(IllegalStateException.class, () -> inventory.reserve(10L, 20L, 1));
    }

    @Test
    @DisplayName("예약 확정 시 총 재고가 확정 수량만큼 감소해야 한다")
    void confirming_reservation_decreases_total_quantity() {
        // given
        Inventory inventory = Inventory.of(1L, 10);
        InventoryReservation reservation = inventory.reserve(10L, 20L, 3);

        // when
        inventory.confirmReservation(reservation);

        // then
        assertEquals(7, inventory.getTotalQuantity());
        assertEquals(InventoryReservationStatus.CONFIRMED, reservation.getStatus());
    }

    @Test
    @DisplayName("예약 해제 시 예약 상태가 해제되고 총 재고는 유지되어야 한다")
    void releasing_reservation_keeps_total_quantity() {
        // given
        Inventory inventory = Inventory.of(1L, 10);
        InventoryReservation reservation = inventory.reserve(10L, 20L, 3);

        // when
        inventory.releaseReservation(reservation);

        // then
        assertEquals(InventoryReservationStatus.RELEASED, reservation.getStatus());
        assertEquals(10, inventory.getTotalQuantity());
    }

    @Test
    @DisplayName("활성 재고는 마감 중 상태로 전환할 수 있어야 한다")
    void active_inventory_can_start_closing() {
        // given
        Inventory inventory = Inventory.of(1L, 10);

        // when
        inventory.startClosing();

        // then
        assertEquals(InventoryStatus.CLOSING, inventory.getStatus());
    }

    @Test
    @DisplayName("마감 중인 재고는 신규 예약을 생성할 수 없어야 한다")
    void closing_inventory_cannot_reserve() {
        // given
        Inventory inventory = Inventory.of(1L, 10);
        inventory.startClosing();

        // when & then
        assertThrows(IllegalStateException.class, () -> inventory.reserve(10L, 20L, 1));
    }

    @Test
    @DisplayName("예약이 남아 있는 재고는 종료할 수 없어야 한다")
    void inventory_with_reserved_reservation_cannot_close() {
        // given
        Inventory inventory = Inventory.of(1L, 10);
        inventory.reserve(10L, 20L, 3);
        inventory.startClosing();

        // when & then
        assertThrows(IllegalStateException.class, inventory::close);
    }

    @Test
    @DisplayName("마감 중인 재고의 기존 예약은 해제할 수 있어야 한다")
    void closing_inventory_can_release_existing_reservation() {
        // given
        Inventory inventory = Inventory.of(1L, 10);
        InventoryReservation reservation = inventory.reserve(10L, 20L, 3);
        inventory.startClosing();

        // when
        inventory.releaseReservation(reservation);

        // then
        assertEquals(InventoryReservationStatus.RELEASED, reservation.getStatus());
    }

    @Test
    @DisplayName("다른 재고에 속한 예약은 처리할 수 없어야 한다")
    void reservation_from_another_inventory_cannot_be_processed() {
        // given
        Inventory inventory = Inventory.of(1L, 10);
        Inventory anotherInventory = Inventory.of(2L, 10);
        InventoryReservation reservation = anotherInventory.reserve(10L, 20L, 3);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> inventory.confirmReservation(reservation));
        assertEquals(InventoryReservationStatus.RESERVED, reservation.getStatus());
    }

    @Test
    @DisplayName("모든 예약이 종료되면 재고를 종료할 수 있어야 한다")
    void closing_inventory_can_close_after_reservations_finish() {
        // given
        Inventory inventory = Inventory.of(1L, 10);
        InventoryReservation reservation = inventory.reserve(10L, 20L, 3);
        inventory.startClosing();
        inventory.releaseReservation(reservation);

        // when
        inventory.close();

        // then
        assertEquals(InventoryStatus.CLOSED, inventory.getStatus());
    }

    @Test
    @DisplayName("종료된 재고는 활성 상태로 재개할 수 있어야 한다")
    void closed_inventory_can_resume() {
        // given
        Inventory inventory = Inventory.of(1L, 10);
        inventory.startClosing();
        inventory.close();

        // when
        inventory.resume();

        // then
        assertEquals(InventoryStatus.ACTIVE, inventory.getStatus());
    }

}
