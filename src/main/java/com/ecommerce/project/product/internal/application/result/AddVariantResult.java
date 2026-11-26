package com.ecommerce.project.product.internal.application.result;

import com.ecommerce.project.product.internal.domain.VariantStatus;

public record AddVariantResult(
        Long productId,
        int price,
        VariantStatus status
) {
}
