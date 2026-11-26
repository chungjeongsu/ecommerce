package com.ecommerce.project.product.internal.application;

import com.ecommerce.project.product.internal.application.command.CreateProductCommand;
import com.ecommerce.project.product.internal.application.result.CreateProductResult;
import com.ecommerce.project.product.internal.application.usecase.CreateProductUseCase;
import com.ecommerce.project.product.internal.domain.Product;
import com.ecommerce.project.product.internal.persist.ProductRepository;
import com.ecommerce.project.user.api.UserApi;
import com.ecommerce.project.user.api.request.AuthorizeSellerReq;
import com.ecommerce.project.user.api.response.AuthorizeSellerRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProductUseCaseImpl implements CreateProductUseCase {

    private final ProductRepository productRepository;
    private final UserApi userApi;

    @Override
    @Transactional
    public CreateProductResult execute(final CreateProductCommand command) {
        ensureCommand(command);
        AuthorizeSellerRes authorization = userApi.authorizeSeller(new AuthorizeSellerReq(command.sellerId()));
        ensureAuthorized(authorization);
        Product product = Product.of(command.sellerId(), command.name(), command.description());
        ensureProductNameNotDuplicated(product);
        Product savedProduct = productRepository.save(product);
        return toResult(savedProduct);
    }

    private void ensureCommand(final CreateProductCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("상품 생성 요청은 필수입니다.");
        }
    }

    private void ensureAuthorized(final AuthorizeSellerRes authorization) {
        if (authorization == null || !authorization.authorized()) {
            throw new IllegalStateException("상품 등록 권한이 없는 판매자입니다.");
        }
    }

    private void ensureProductNameNotDuplicated(final Product product) {
        if (productRepository.findBySellerIdAndName(product.getSellerId(), product.getName()).isPresent()) {
            throw new IllegalStateException("같은 판매자는 동일한 상품명을 사용할 수 없습니다.");
        }
    }

    private CreateProductResult toResult(final Product product) {
        return new CreateProductResult(
                product.getId(),
                product.getSellerId(),
                product.getName(),
                product.getDescription(),
                product.getStatus()
        );
    }
}
