package com.ecommerce.project.order.internal.persist;

import com.ecommerce.project.order.internal.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
