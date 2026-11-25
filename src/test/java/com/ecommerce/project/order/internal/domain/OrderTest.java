package com.ecommerce.project.order.internal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    @DisplayName("주문은 하나 이상의 주문 항목을 포함하고 준비 상태로 생성된다")
    void order_starts_ready_with_items() {
        // given
        UUID orderCode = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 25, 10, 0);
        LocalDateTime expiresAt = createdAt.plusMinutes(15);
        OrderItem orderItem = OrderItem.of(1L, "노트북 거치대", "색상: 실버", 30_000L, 2);

        // when
        Order order = Order.of(orderCode, 10L, List.of(orderItem), createdAt, expiresAt);

        // then
        assertEquals(orderCode, order.getOrderCode());
        assertEquals(10L, order.getBuyerId());
        assertEquals(OrderStatus.READY, order.getStatus());
        assertEquals(List.of(orderItem), order.getItems());
        assertEquals(createdAt, order.getCreatedAt());
        assertEquals(expiresAt, order.getExpiresAt());
    }

    @Test
    @DisplayName("주문에는 하나 이상의 주문 항목이 필요하다")
    void order_requires_at_least_one_item() {
        // given
        UUID orderCode = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 25, 10, 0);

        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> Order.of(orderCode, 10L, List.of(), createdAt, createdAt.plusMinutes(15))
        );
    }

    @Test
    @DisplayName("같은 주문에는 동일한 Variant를 중복해서 포함할 수 없다")
    void order_rejects_duplicated_variant() {
        // given
        UUID orderCode = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 25, 10, 0);
        OrderItem first = OrderItem.of(1L, "노트북 거치대", "색상: 실버", 30_000L, 1);
        OrderItem duplicated = OrderItem.of(1L, "노트북 거치대", "색상: 실버", 30_000L, 2);

        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> Order.of(
                        orderCode,
                        10L,
                        List.of(first, duplicated),
                        createdAt,
                        createdAt.plusMinutes(15)
                )
        );
    }

    @Test
    @DisplayName("주문 총액은 각 주문 항목 금액의 합으로 계산한다")
    void order_total_amount_is_sum_of_item_amounts() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 25, 10, 0);
        OrderItem first = OrderItem.of(1L, "노트북 거치대", "색상: 실버", 30_000L, 2);
        OrderItem second = OrderItem.of(2L, "키보드", "축: 갈축", 100_000L, 1);
        Order order = Order.of(
                UUID.randomUUID(),
                10L,
                List.of(first, second),
                createdAt,
                createdAt.plusMinutes(15)
        );

        // when
        long totalAmount = order.getTotalAmount();

        // then
        assertEquals(160_000L, totalAmount);
    }

    @Test
    @DisplayName("주문 항목 컬렉션은 생성 후 변경할 수 없다")
    void order_items_are_immutable_after_creation() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 25, 10, 0);
        OrderItem orderItem = OrderItem.of(1L, "노트북 거치대", "색상: 실버", 30_000L, 1);
        List<OrderItem> requestedItems = new ArrayList<>(List.of(orderItem));
        Order order = Order.of(
                UUID.randomUUID(),
                10L,
                requestedItems,
                createdAt,
                createdAt.plusMinutes(15)
        );

        // when
        requestedItems.clear();

        // then
        assertEquals(List.of(orderItem), order.getItems());
        assertThrows(UnsupportedOperationException.class, () -> order.getItems().clear());
    }

    @Test
    @DisplayName("준비된 주문은 결제 완료 상태로 전환할 수 있다")
    void ready_order_can_be_paid() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 25, 10, 0);
        Order order = Order.of(
                UUID.randomUUID(),
                10L,
                List.of(OrderItem.of(1L, "노트북 거치대", null, 30_000L, 1)),
                createdAt,
                createdAt.plusMinutes(15)
        );

        // when
        order.pay();

        // then
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertThrows(IllegalStateException.class, order::pay);
    }

    @Test
    @DisplayName("준비된 주문은 취소 상태로 전환할 수 있다")
    void ready_order_can_be_cancelled() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 25, 10, 0);
        Order order = Order.of(
                UUID.randomUUID(),
                10L,
                List.of(OrderItem.of(1L, "노트북 거치대", null, 30_000L, 1)),
                createdAt,
                createdAt.plusMinutes(15)
        );

        // when
        order.cancel();

        // then
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertThrows(IllegalStateException.class, order::cancel);
    }

    @Test
    @DisplayName("결제 기한에 도달한 준비 상태 주문은 만료할 수 있다")
    void ready_order_can_expire_at_deadline() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 25, 10, 0);
        LocalDateTime expiresAt = createdAt.plusMinutes(15);
        Order order = Order.of(
                UUID.randomUUID(),
                10L,
                List.of(OrderItem.of(1L, "노트북 거치대", null, 30_000L, 1)),
                createdAt,
                expiresAt
        );

        // when
        order.expire(expiresAt);

        // then
        assertEquals(OrderStatus.EXPIRED, order.getStatus());
    }

    @Test
    @DisplayName("결제 기한 전에는 주문을 만료할 수 없다")
    void order_cannot_expire_before_deadline() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 25, 10, 0);
        LocalDateTime expiresAt = createdAt.plusMinutes(15);
        Order order = Order.of(
                UUID.randomUUID(),
                10L,
                List.of(OrderItem.of(1L, "노트북 거치대", null, 30_000L, 1)),
                createdAt,
                expiresAt
        );

        // when & then
        assertThrows(IllegalStateException.class, () -> order.expire(expiresAt.minusNanos(1)));
        assertEquals(OrderStatus.READY, order.getStatus());
    }

    @Test
    @DisplayName("주문 만료 시각은 생성 시각보다 이후여야 한다")
    void order_expiration_must_be_after_creation() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 25, 10, 0);
        OrderItem orderItem = OrderItem.of(1L, "노트북 거치대", null, 30_000L, 1);

        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> Order.of(UUID.randomUUID(), 10L, List.of(orderItem), createdAt, createdAt)
        );
    }
}
