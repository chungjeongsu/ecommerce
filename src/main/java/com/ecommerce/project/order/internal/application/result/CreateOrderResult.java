package com.ecommerce.project.order.internal.application.result;
public sealed interface CreateOrderResult permits CreateOrderSuccessResult, CreateOrderFailureResult {}
