package com.ecommerce.project.order.internal.application.result;
import java.util.List;
public record CreateOrderFailureResult(
        List<Shortage> shortages
) implements CreateOrderResult {

    public CreateOrderFailureResult {
        shortages = List.copyOf(shortages);
    }

    public record Shortage(
            Long variantId,
            String displayName,
            int requestedQuantity,
            int availableQuantity
    ) {}
}
