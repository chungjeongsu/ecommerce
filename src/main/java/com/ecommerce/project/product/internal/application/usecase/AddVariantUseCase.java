package com.ecommerce.project.product.internal.application.usecase;

import com.ecommerce.project.product.internal.application.command.AddVariantCommand;
import com.ecommerce.project.product.internal.application.result.AddVariantResult;

public interface AddVariantUseCase {

    AddVariantResult execute(AddVariantCommand command);
}
