package com.ecommerce.project.product.internal.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.project.product.internal.application.command.AddVariantCommand;
import com.ecommerce.project.product.internal.application.result.AddVariantResult;
import com.ecommerce.project.product.internal.domain.OptionValue;
import com.ecommerce.project.product.internal.domain.Product;
import com.ecommerce.project.product.internal.domain.Variant;
import com.ecommerce.project.product.internal.domain.VariantStatus;
import com.ecommerce.project.product.internal.persist.ProductRepository;
import com.ecommerce.project.user.api.UserApi;
import com.ecommerce.project.user.api.request.AuthorizeSellerReq;
import com.ecommerce.project.user.api.response.AuthorizeSellerRes;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddVariantUseCaseImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserApi userApi;

    @InjectMocks
    private AddVariantUseCaseImpl useCase;

    @Test
    @DisplayName("null 요청으로는 Variant를 추가할 수 없다")
    void null_command_cannot_add_variant() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
        verify(userApi, never()).authorizeSeller(any(AuthorizeSellerReq.class));
        verify(productRepository, never()).findForChange(any());
    }

    @Test
    @DisplayName("sellerId가 없으면 Variant를 추가할 수 없다")
    void null_seller_id_cannot_add_variant() {
        // given
        AddVariantCommand command = new AddVariantCommand(null, 10L, 10_000, List.of());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
        verify(userApi, never()).authorizeSeller(any());
    }

    @Test
    @DisplayName("productId가 없으면 Variant를 추가할 수 없다")
    void null_product_id_cannot_add_variant() {
        // given
        AddVariantCommand command = new AddVariantCommand(1L, null, 10_000, List.of());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
        verify(userApi, never()).authorizeSeller(any());
    }

    @Test
    @DisplayName("OptionValue ID 목록이 없으면 Variant를 추가할 수 없다")
    void null_option_value_ids_cannot_add_variant() {
        // given
        AddVariantCommand command = new AddVariantCommand(1L, 10L, 10_000, null);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
        verify(userApi, never()).authorizeSeller(any());
    }

    @Test
    @DisplayName("null 권한 응답으로는 Variant를 추가할 수 없다")
    void null_authorization_cannot_add_variant() {
        // given
        AddVariantCommand command = new AddVariantCommand(1L, 10L, 10_000, List.of());
        when(userApi.authorizeSeller(any(AuthorizeSellerReq.class))).thenReturn(null);

        // when & then
        assertThrows(IllegalStateException.class, () -> useCase.execute(command));
        verify(productRepository, never()).findForChange(any());
    }

    @Test
    @DisplayName("권한이 없으면 Variant를 추가할 수 없다")
    void unauthorized_seller_cannot_add_variant() {
        // given
        AddVariantCommand command = new AddVariantCommand(1L, 10L, 10_000, List.of());
        when(userApi.authorizeSeller(any(AuthorizeSellerReq.class)))
                .thenReturn(new AuthorizeSellerRes(false));

        // when & then
        assertThrows(IllegalStateException.class, () -> useCase.execute(command));
        verify(productRepository, never()).findForChange(any());
    }

    @Test
    @DisplayName("존재하지 않는 Product에는 Variant를 추가할 수 없다")
    void missing_product_cannot_add_variant() {
        // given
        AddVariantCommand command = new AddVariantCommand(1L, 10L, 10_000, List.of());
        when(userApi.authorizeSeller(any(AuthorizeSellerReq.class)))
                .thenReturn(new AuthorizeSellerRes(true));
        when(productRepository.findForChange(10L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    @DisplayName("상품에 속하지 않은 OptionValue로는 Variant를 추가할 수 없다")
    void foreign_option_value_cannot_add_variant() {
        // given
        Product product = org.mockito.Mockito.mock(Product.class);
        OptionValue optionValue = org.mockito.Mockito.mock(OptionValue.class);
        AddVariantCommand command = new AddVariantCommand(1L, 10L, 10_000, List.of(99L));
        when(userApi.authorizeSeller(any(AuthorizeSellerReq.class)))
                .thenReturn(new AuthorizeSellerRes(true));
        when(productRepository.findForChange(10L)).thenReturn(Optional.of(product));
        when(product.getSellerId()).thenReturn(1L);
        when(product.findOptionValues(List.of(99L)))
                .thenThrow(new IllegalArgumentException());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
        verify(product, never()).addVariant(any(Integer.class), any());
    }

    @Test
    @DisplayName("상품에 속한 OptionValue 조합으로 Variant를 생성하고 결과를 반환한다")
    void adds_variant_with_product_option_values() {
        // given
        Product product = org.mockito.Mockito.mock(Product.class);
        OptionValue optionValue = org.mockito.Mockito.mock(OptionValue.class);
        Variant variant = org.mockito.Mockito.mock(Variant.class);
        AddVariantCommand command = new AddVariantCommand(1L, 10L, 10_000, List.of(1L));
        when(userApi.authorizeSeller(any(AuthorizeSellerReq.class)))
                .thenReturn(new AuthorizeSellerRes(true));
        when(productRepository.findForChange(10L)).thenReturn(Optional.of(product));
        when(product.getSellerId()).thenReturn(1L);
        when(product.getId()).thenReturn(10L);
        when(product.findOptionValues(List.of(1L))).thenReturn(List.of(optionValue));
        when(product.addVariant(10_000, List.of(optionValue))).thenReturn(variant);
        when(variant.getPrice()).thenReturn(10_000);
        when(variant.getStatus()).thenReturn(VariantStatus.STOPPED);

        // when
        AddVariantResult result = useCase.execute(command);

        // then
        verify(product).addVariant(eq(10_000), eq(List.of(optionValue)));
        assertEquals(10L, result.productId());
        assertEquals(10_000, result.price());
        assertEquals(VariantStatus.STOPPED, result.status());
    }

    @Test
    @DisplayName("OptionValue ID가 중복되면 Variant를 추가할 수 없다")
    void duplicated_option_value_id_cannot_add_variant() {
        // given
        Product product = Product.of(1L, "테스트상품", null);
        AddVariantCommand command = new AddVariantCommand(1L, 10L, 10_000, List.of(1L, 1L));
        when(userApi.authorizeSeller(any(AuthorizeSellerReq.class)))
                .thenReturn(new AuthorizeSellerRes(true));
        when(productRepository.findForChange(10L)).thenReturn(Optional.of(product));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
        assertEquals(0, product.getVariants().size());
    }

    @Test
    @DisplayName("상품 소유 판매자는 Variant를 추가할 수 있다")
    void owner_can_add_variant() {
        // given
        Product product = Product.of(1L, "노트북 거치대", null);
        AddVariantCommand command = new AddVariantCommand(1L, 10L, 10_000, List.of());
        when(userApi.authorizeSeller(any(AuthorizeSellerReq.class)))
                .thenReturn(new AuthorizeSellerRes(true));
        when(productRepository.findForChange(10L)).thenReturn(Optional.of(product));

        // when
        AddVariantResult result = useCase.execute(command);

        // then
        assertEquals(10_000, result.price());
        verify(productRepository, never()).save(any(Product.class));
        assertEquals(1, product.getVariants().size());
    }

    @Test
    @DisplayName("상품 소유자가 아니면 Variant를 추가할 수 없다")
    void non_owner_cannot_add_variant() {
        // given
        Product product = Product.of(2L, "노트북 거치대", null);
        AddVariantCommand command = new AddVariantCommand(1L, 10L, 10_000, List.of());
        when(userApi.authorizeSeller(any(AuthorizeSellerReq.class)))
                .thenReturn(new AuthorizeSellerRes(true));
        when(productRepository.findForChange(10L)).thenReturn(Optional.of(product));

        // when & then
        assertThrows(IllegalStateException.class, () -> useCase.execute(command));
        assertEquals(0, product.getVariants().size());
    }

    @Test
    @DisplayName("가격이 음수이면 Variant를 추가할 수 없다")
    void negative_price_cannot_add_variant() {
        // given
        Product product = Product.of(1L, "노트북 거치대", null);
        AddVariantCommand command = new AddVariantCommand(1L, 10L, -1, List.of());
        when(userApi.authorizeSeller(any(AuthorizeSellerReq.class)))
                .thenReturn(new AuthorizeSellerRes(true));
        when(productRepository.findForChange(10L)).thenReturn(Optional.of(product));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
        assertEquals(0, product.getVariants().size());
    }
}
