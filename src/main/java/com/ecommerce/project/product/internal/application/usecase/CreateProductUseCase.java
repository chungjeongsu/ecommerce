package com.ecommerce.project.product.internal.application.usecase;

import com.ecommerce.project.product.internal.application.command.CreateProductCommand;
import com.ecommerce.project.product.internal.application.result.CreateProductResult;

public interface CreateProductUseCase {

    CreateProductResult execute(CreateProductCommand command);
}
