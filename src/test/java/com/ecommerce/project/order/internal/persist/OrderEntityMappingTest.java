package com.ecommerce.project.order.internal.persist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ecommerce.project.order.internal.domain.Order;
import com.ecommerce.project.order.internal.domain.OrderItem;
import com.ecommerce.project.support.MySqlDataJpaTest;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@MySqlDataJpaTest
class OrderEntityMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("주문과 주문 항목을 함께 저장하고 조회할 수 있다")
    void order_and_items_can_be_persisted() {
        // given
        UUID orderCode = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 25, 10, 0);
        Order order = Order.of(
                orderCode,
                10L,
                List.of(
                        OrderItem.of(1L, "노트북 거치대", "색상: 실버", 30_000L, 2),
                        OrderItem.of(2L, "키보드", null, 100_000L, 1)
                ),
                createdAt,
                createdAt.plusMinutes(15)
        );

        // when
        entityManager.persist(order);
        entityManager.flush();
        Long orderId = order.getId();
        entityManager.clear();
        Order foundOrder = entityManager.find(Order.class, orderId);

        // then
        assertNotNull(orderId);
        assertEquals(orderCode, foundOrder.getOrderCode());
        assertEquals(2, foundOrder.getItems().size());
        assertEquals(160_000L, foundOrder.getTotalAmount());
    }
}
