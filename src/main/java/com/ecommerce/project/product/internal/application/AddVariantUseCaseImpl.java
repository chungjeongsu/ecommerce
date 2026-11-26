package com.ecommerce.project.product.internal.application;

import com.ecommerce.project.product.internal.application.command.AddVariantCommand;
import com.ecommerce.project.product.internal.application.result.AddVariantResult;
import com.ecommerce.project.product.internal.application.usecase.AddVariantUseCase;
import com.ecommerce.project.product.internal.domain.OptionValue;
import com.ecommerce.project.product.internal.domain.Product;
import com.ecommerce.project.product.internal.domain.Variant;
import com.ecommerce.project.product.internal.persist.ProductRepository;
import com.ecommerce.project.user.api.UserApi;
import com.ecommerce.project.user.api.request.AuthorizeSellerReq;
import com.ecommerce.project.user.api.response.AuthorizeSellerRes;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddVariantUseCaseImpl implements AddVariantUseCase {

    private final ProductRepository productRepository;
    private final UserApi userApi;

    @Override
    @Transactional
    public AddVariantResult execute(final AddVariantCommand command) {
        ensureCommand(command);
        AuthorizeSellerRes authorization = userApi.authorizeSeller(new AuthorizeSellerReq(command.sellerId()));
        ensureAuthorized(authorization);
        Product product = productRepository.findForChange(command.productId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        ensureProductOwner(product, command.sellerId());

        List<OptionValue> optionValues = product.findOptionValues(command.optionValueIds());
        Variant variant = product.addVariant(command.price(), optionValues);
        return new AddVariantResult(product.getId(), variant.getPrice(), variant.getStatus());
    }

    private void ensureCommand(final AddVariantCommand command) {
        if (command == null || command.sellerId() == null || command.productId() == null
                || command.optionValueIds() == null) {
            throw new IllegalArgumentException("Variant 생성 요청이 올바르지 않습니다.");
        }
    }

    private void ensureAuthorized(final AuthorizeSellerRes authorization) {
        if (authorization == null || !authorization.authorized()) {
            throw new IllegalStateException("상품 관리 권한이 없는 판매자입니다.");
        }
    }

    private void ensureProductOwner(final Product product, final Long sellerId) {
        if (!product.getSellerId().equals(sellerId)) {
            throw new IllegalStateException("상품 소유자만 Variant를 추가할 수 있습니다.");
        }
    }
}