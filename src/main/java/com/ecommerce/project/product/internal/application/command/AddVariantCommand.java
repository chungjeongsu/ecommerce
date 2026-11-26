package com.ecommerce.project.product.internal.application.command;

import java.util.List;

public record AddVariantCommand(
        Long sellerId,
        Long productId,
        int price,
        List<Long> optionValueIds
) {
}
