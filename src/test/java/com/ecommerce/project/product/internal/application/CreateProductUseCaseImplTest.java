package com.ecommerce.project.product.internal.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.project.product.internal.application.command.CreateProductCommand;
import com.ecommerce.project.product.internal.application.result.CreateProductResult;
import com.ecommerce.project.product.internal.domain.Product;
import com.ecommerce.project.product.internal.domain.ProductStatus;
import com.ecommerce.project.product.internal.persist.ProductRepository;
import com.ecommerce.project.user.api.UserApi;
import com.ecommerce.project.user.api.request.AuthorizeSellerReq;
import com.ecommerce.project.user.api.response.AuthorizeSellerRes;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserApi userApi;

    @InjectMocks
    private CreateProductUseCaseImpl useCase;

    @Captor
    private ArgumentCaptor<AuthorizeSellerReq> authorizationRequestCaptor;

    @Test
    @DisplayName("판매자 권한을 확인하고 중지 상태의 상품을 저장한다")
    void creates_stopped_product_for_authorized_seller() {
        // given
        CreateProductCommand command = new CreateProductCommand(1L, "노트북 거치대", "알루미늄 거치대");
        Product product = Product.of(1L, command.name(), command.description());
        when(userApi.authorizeSeller(any(AuthorizeSellerReq.class)))
                .thenReturn(new AuthorizeSellerRes(true));
        when(productRepository.findBySellerIdAndName(1L, command.name()))
                .thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // when
        CreateProductResult result = useCase.execute(command);

        // then
        verify(userApi).authorizeSeller(authorizationRequestCaptor.capture());
        assertEquals(1L, authorizationRequestCaptor.getValue().userId());
        verify(productRepository).save(any(Product.class));
        assertEquals(1L, result.sellerId());
        assertEquals("노트북 거치대", result.name());
        assertEquals(ProductStatus.STOPPED, result.status());
    }

    @Test
    @DisplayName("판매자 권한이 없으면 상품을 저장하지 않는다")
    void rejects_unauthorized_seller() {
        // given
        CreateProductCommand command = new CreateProductCommand(1L, "노트북 거치대", null);
        when(userApi.authorizeSeller(any(AuthorizeSellerReq.class)))
                .thenReturn(new AuthorizeSellerRes(false));

        // when & then
        assertThrows(IllegalStateException.class, () -> useCase.execute(command));
        verify(productRepository, never()).findBySellerIdAndName(any(), any());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 생성 요청이 없으면 상품을 생성할 수 없다")
    void rejects_null_command() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
        verify(userApi, never()).authorizeSeller(any());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("User API 응답이 없으면 상품을 생성할 수 없다")
    void rejects_null_authorization_response() {
        // given
        CreateProductCommand command = new CreateProductCommand(1L, "노트북 거치대", null);
        when(userApi.authorizeSeller(any(AuthorizeSellerReq.class))).thenReturn(null);

        // when & then
        assertThrows(IllegalStateException.class, () -> useCase.execute(command));
        verify(productRepository, never()).findBySellerIdAndName(any(), any());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("같은 판매자의 동일 상품명이 있으면 상품을 저장하지 않는다")
    void rejects_duplicated_product_name_for_seller() {
        // given
        CreateProductCommand command = new CreateProductCommand(1L, "노트북 거치대", null);
        when(userApi.authorizeSeller(any(AuthorizeSellerReq.class)))
                .thenReturn(new AuthorizeSellerRes(true));
        when(productRepository.findBySellerIdAndName(1L, command.name()))
                .thenReturn(Optional.of(Product.of(1L, command.name(), null)));

        // when & then
        assertThrows(IllegalStateException.class, () -> useCase.execute(command));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("상품명 앞뒤 공백은 제거된 이름으로 중복 확인한다")
    void checks_duplicate_with_trimmed_product_name() {
        // given
        CreateProductCommand command = new CreateProductCommand(1L, " 노트북 거치대 ", null);
        when(userApi.authorizeSeller(any(AuthorizeSellerReq.class)))
                .thenReturn(new AuthorizeSellerRes(true));
        when(productRepository.findBySellerIdAndName(eq(1L), eq("노트북 거치대")))
                .thenReturn(Optional.of(Product.of(1L, "노트북 거치대", null)));

        // when & then
        assertThrows(IllegalStateException.class, () -> useCase.execute(command));
        verify(productRepository, never()).save(any(Product.class));
    }
}
