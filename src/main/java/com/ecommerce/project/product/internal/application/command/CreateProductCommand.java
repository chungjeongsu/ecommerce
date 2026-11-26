package com.ecommerce.project.product.internal.application.command;

public record CreateProductCommand(
        Long sellerId,
        String name,
        String description
) {
}
