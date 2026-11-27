package com.ecommerce.project.order.internal.application.result;

public record CreateOrderSuccessResult(
        Long orderId
) implements CreateOrderResult {}
