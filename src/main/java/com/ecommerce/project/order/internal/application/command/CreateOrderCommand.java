package com.ecommerce.project.order.internal.application.command;

import java.util.List;

public record CreateOrderCommand(
        Long buyerId,
        List<Item> items
) {
    public record Item(
            Long variantId,
            int quantity
    ) {}
}
