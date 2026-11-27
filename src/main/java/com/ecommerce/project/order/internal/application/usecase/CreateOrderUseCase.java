package com.ecommerce.project.order.internal.application.usecase;

import com.ecommerce.project.order.internal.application.command.CreateOrderCommand;
import com.ecommerce.project.order.internal.application.result.CreateOrderResult;

public interface CreateOrderUseCase {
    CreateOrderResult execute(CreateOrderCommand command);
}
