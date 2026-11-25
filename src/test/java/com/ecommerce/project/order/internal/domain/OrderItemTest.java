package com.ecommerce.project.order.internal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderItemTest {

    @Test
    @DisplayName("주문 항목은 주문 당시 상품 정보와 수량을 보존한다")
    void order_item_preserves_ordered_product_information() {
        // given
        OrderItem orderItem = OrderItem.of(1L, "노트북 거치대", "색상: 실버", 30_000L, 2);

        // when
        long amount = orderItem.getAmount();

        // then
        assertEquals(1L, orderItem.getVariantId());
        assertEquals("노트북 거치대", orderItem.getProductName());
        assertEquals("색상: 실버", orderItem.getOptionCombination());
        assertEquals(30_000L, orderItem.getUnitPrice());
        assertEquals(2, orderItem.getQuantity());
        assertEquals(60_000L, amount);
    }

    @Test
    @DisplayName("주문 항목 금액은 long 범위로 계산한다")
    void order_item_amount_uses_long() {
        // given
        OrderItem orderItem = OrderItem.of(1L, "고가 상품", null, 3_000_000_000L, 2);

        // when
        long amount = orderItem.getAmount();

        // then
        assertEquals(6_000_000_000L, amount);
    }

    @Test
    @DisplayName("주문 수량은 1개와 100개를 허용한다")
    void order_item_quantity_accepts_boundaries() {
        // given
        OrderItem minimum = OrderItem.of(1L, "최소 수량 상품", null, 1_000L, 1);
        OrderItem maximum = OrderItem.of(2L, "최대 수량 상품", null, 1_000L, 100);

        // when
        int minimumQuantity = minimum.getQuantity();
        int maximumQuantity = maximum.getQuantity();

        // then
        assertEquals(1, minimumQuantity);
        assertEquals(100, maximumQuantity);
    }

    @Test
    @DisplayName("주문 수량은 1개 미만이거나 100개를 초과할 수 없다")
    void order_item_quantity_must_be_between_one_and_one_hundred() {
        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> OrderItem.of(1L, "수량 부족 상품", null, 1_000L, 0)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> OrderItem.of(2L, "수량 초과 상품", null, 1_000L, 101)
        );
    }

    @Test
    @DisplayName("주문 당시 단가는 0원 이상이어야 한다")
    void order_item_unit_price_must_not_be_negative() {
        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> OrderItem.of(1L, "잘못된 가격 상품", null, -1L, 1)
        );
    }

    @Test
    @DisplayName("주문 항목에는 Variant 식별자와 상품명이 필요하다")
    void order_item_requires_variant_id_and_product_name() {
        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> OrderItem.of(null, "상품", null, 1_000L, 1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> OrderItem.of(1L, " ", null, 1_000L, 1)
        );
    }
}
