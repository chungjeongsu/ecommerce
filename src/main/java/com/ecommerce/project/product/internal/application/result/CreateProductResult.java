package com.ecommerce.project.product.internal.application.result;

import com.ecommerce.project.product.internal.domain.ProductStatus;

public record CreateProductResult(
        Long productId,
        Long sellerId,
        String name,
        String description,
        ProductStatus status
) {
}
