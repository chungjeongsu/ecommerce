package com.ecommerce.project.order.internal.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ecommerce.project.inventory.internal.domain.Inventory;
import com.ecommerce.project.inventory.internal.persist.InventoryRepository;
import com.ecommerce.project.order.internal.application.command.CreateOrderCommand;
import com.ecommerce.project.order.internal.application.result.CreateOrderFailureResult;
import com.ecommerce.project.order.internal.application.result.CreateOrderSuccessResult;
import com.ecommerce.project.order.internal.domain.Order;
import com.ecommerce.project.order.internal.domain.OrderItem;
import com.ecommerce.project.order.internal.persist.OrderRepository;
import com.ecommerce.project.product.internal.domain.OptionValue;
import com.ecommerce.project.product.internal.domain.Product;
import com.ecommerce.project.product.internal.domain.ProductStatus;
import com.ecommerce.project.product.internal.domain.Variant;
import com.ecommerce.project.product.internal.persist.ProductRepository;
import com.ecommerce.project.user.api.UserApi;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseImplTest {
    private static final Long BUYER_ID = 1L;
    private static final Long VARIANT_ID = 10L;
    private static final int UNIT_PRICE = 1_000;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserApi userApi;

    @Mock
    private Variant variant;

    @Mock
    private Product product;

    @Mock
    private Inventory inventory;

    @Mock
    private Order savedOrder;

    private CreateOrderUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateOrderUseCaseImpl(
                productRepository,
                inventoryRepository,
                orderRepository,
                userApi,
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    @DisplayName("null 주문 요청은 거부한다")
    void null_command_is_rejected() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
        verifyNoInteractions(userApi, productRepository, inventoryRepository, orderRepository);
    }

    @Test
    @DisplayName("구매자 ID가 없으면 주문 요청을 거부한다")
    void missing_buyer_id_is_rejected() {
        // given
        CreateOrderCommand command = new CreateOrderCommand(null, List.of(new CreateOrderCommand.Item(VARIANT_ID, 1)));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
        verifyNoInteractions(userApi, productRepository, inventoryRepository, orderRepository);
    }

    @Test
    @DisplayName("주문 상품이 없으면 구매자 조회 전에 요청을 거부한다")
    void empty_items_are_rejected_before_user_lookup() {
        // given
        CreateOrderCommand command = new CreateOrderCommand(BUYER_ID, List.of());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
        verifyNoInteractions(userApi, productRepository, inventoryRepository, orderRepository);
    }

    @Test
    @DisplayName("활성 구매자가 아니면 주문을 생성하지 않는다")
    void inactive_buyer_cannot_execute_order() {
        // given
        when(userApi.existsActiveBuyer(BUYER_ID)).thenReturn(false);
        CreateOrderCommand command = orderCommand(1);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
        verifyNoInteractions(productRepository, inventoryRepository, orderRepository);
    }

    @Test
    @DisplayName("null 주문 상품은 거부한다")
    void null_item_is_rejected() {
        // given
        givenActiveBuyer();
        CreateOrderCommand command = new CreateOrderCommand(BUYER_ID, Collections.singletonList(null));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
        verifyNoInteractions(productRepository, inventoryRepository, orderRepository);
    }

    @Test
    @DisplayName("주문 수량이 1보다 작으면 주문을 생성하지 않는다")
    void quantity_below_minimum_is_rejected() {
        // given
        givenActiveBuyer();
        givenOrderableItem(10);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(orderCommand(0)));
        verify(orderRepository, never()).saveAndFlush(any(Order.class));
    }

    @Test
    @DisplayName("주문 수량이 100보다 크면 주문을 생성하지 않는다")
    void quantity_above_maximum_is_rejected() {
        // given
        givenActiveBuyer();
        givenOrderableItem(101);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(orderCommand(101)));
        verify(orderRepository, never()).saveAndFlush(any(Order.class));
    }

    @Test
    @DisplayName("존재하지 않는 Variant는 주문할 수 없다")
    void missing_variant_is_rejected() {
        // given
        givenActiveBuyer();
        when(productRepository.findVariantForOrder(VARIANT_ID)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(orderCommand(1)));
        verifyNoInteractions(inventoryRepository, orderRepository);
    }

    @Test
    @DisplayName("판매 중지 상품은 주문할 수 없다")
    void stopped_product_cannot_be_ordered() {
        // given
        givenActiveBuyer();
        when(productRepository.findVariantForOrder(VARIANT_ID)).thenReturn(Optional.of(variant));
        when(variant.getProduct()).thenReturn(product);
        when(product.getStatus()).thenReturn(ProductStatus.STOPPED);

        // when & then
        assertThrows(IllegalStateException.class, () -> useCase.execute(orderCommand(1)));
        verifyNoInteractions(inventoryRepository, orderRepository);
    }

    @Test
    @DisplayName("비활성 Variant는 주문할 수 없다")
    void inactive_variant_cannot_be_ordered() {
        // given
        givenActiveBuyer();
        when(productRepository.findVariantForOrder(VARIANT_ID)).thenReturn(Optional.of(variant));
        when(variant.getProduct()).thenReturn(product);
        when(product.getStatus()).thenReturn(ProductStatus.ON_SALE);
        when(variant.isActive()).thenReturn(false);

        // when & then
        assertThrows(IllegalStateException.class, () -> useCase.execute(orderCommand(1)));
        verifyNoInteractions(inventoryRepository, orderRepository);
    }

    @Test
    @DisplayName("재고가 없으면 주문을 생성할 수 없다")
    void missing_inventory_is_rejected() {
        // given
        givenActiveBuyer();
        when(productRepository.findVariantForOrder(VARIANT_ID)).thenReturn(Optional.of(variant));
        when(variant.getProduct()).thenReturn(product);
        when(product.getStatus()).thenReturn(ProductStatus.ON_SALE);
        when(variant.isActive()).thenReturn(true);
        when(inventoryRepository.findByVariantId(VARIANT_ID)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalStateException.class, () -> useCase.execute(orderCommand(1)));
        verify(orderRepository, never()).saveAndFlush(any(Order.class));
    }

    @Test
    @DisplayName("재고가 부족하면 부족한 상품 정보를 반환하고 주문을 저장하지 않는다")
    void shortage_returns_failure_without_saving_order() {
        // given
        givenActiveBuyer();
        givenOrderableItem(2);
        when(inventory.availableQuantity()).thenReturn(0);

        // when
        CreateOrderFailureResult result = assertInstanceOf(
                CreateOrderFailureResult.class,
                useCase.execute(orderCommand(2))
        );

        // then
        assertEquals(1, result.shortages().size());
        assertEquals(VARIANT_ID, result.shortages().get(0).variantId());
        assertEquals("상품", result.shortages().get(0).displayName());
        assertEquals(2, result.shortages().get(0).requestedQuantity());
        assertEquals(0, result.shortages().get(0).availableQuantity());
        verify(orderRepository, never()).saveAndFlush(any(Order.class));
        verify(inventory, never()).reserve(any(), any(), any(Integer.class));
    }

    @Test
    @DisplayName("여러 상품의 재고 부족 정보를 모두 반환한다")
    void all_shortages_are_returned_for_multiple_items() {
        // given
        givenActiveBuyer();
        givenOrderableItem(2);
        when(inventory.availableQuantity()).thenReturn(0);

        Variant secondVariant = mock(Variant.class);
        Product secondProduct = mock(Product.class);
        Inventory secondInventory = mock(Inventory.class);
        givenOrderableItem(20L, secondVariant, secondProduct, secondInventory, "두 번째 상품", 2_000, 1);

        // when
        CreateOrderFailureResult result = assertInstanceOf(
                CreateOrderFailureResult.class,
                useCase.execute(new CreateOrderCommand(BUYER_ID, List.of(
                        new CreateOrderCommand.Item(VARIANT_ID, 2),
                        new CreateOrderCommand.Item(20L, 3)
                )))
        );

        // then
        assertEquals(2, result.shortages().size());
        assertEquals(VARIANT_ID, result.shortages().get(0).variantId());
        assertEquals(2, result.shortages().get(0).requestedQuantity());
        assertEquals(20L, result.shortages().get(1).variantId());
        assertEquals("두 번째 상품", result.shortages().get(1).displayName());
        assertEquals(3, result.shortages().get(1).requestedQuantity());
        assertEquals(1, result.shortages().get(1).availableQuantity());
        verify(orderRepository, never()).saveAndFlush(any(Order.class));
    }

    @Test
    @DisplayName("동일한 Variant를 중복해서 주문할 수 없다")
    void duplicated_variant_is_rejected_by_order_aggregate() {
        // given
        givenActiveBuyer();
        givenOrderableItem(1);
        when(inventory.availableQuantity()).thenReturn(10);

        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(new CreateOrderCommand(BUYER_ID, List.of(
                        new CreateOrderCommand.Item(VARIANT_ID, 1),
                        new CreateOrderCommand.Item(VARIANT_ID, 1)
                )))
        );
        verify(orderRepository, never()).saveAndFlush(any(Order.class));
    }

    @Test
    @DisplayName("가용 재고가 있으면 주문을 저장하고 주문 항목 ID로 재고를 예약한다")
    void available_inventory_creates_order_and_reservation() {
        // given
        givenActiveBuyer();
        givenOrderableItem(2);
        when(inventory.availableQuantity()).thenReturn(5);
        givenSavedOrder(20L, 30L);

        // when
        CreateOrderSuccessResult result = assertInstanceOf(
                CreateOrderSuccessResult.class,
                useCase.execute(orderCommand(2))
        );

        // then
        assertEquals(20L, result.orderId());
        verify(inventory).reserve(20L, 30L, 2);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).saveAndFlush(orderCaptor.capture());
        assertEquals(BUYER_ID, orderCaptor.getValue().getBuyerId());
        assertEquals("상품", orderCaptor.getValue().getItems().get(0).getProductName());
        assertEquals(2, orderCaptor.getValue().getItems().get(0).getQuantity());
        assertEquals(UNIT_PRICE, orderCaptor.getValue().getItems().get(0).getUnitPrice());
    }

    @Test
    @DisplayName("주문 항목은 주문 당시 옵션 조합을 보존한다")
    void order_item_preserves_option_combination() {
        // given
        givenActiveBuyer();
        givenOrderableItem(1);
        when(inventory.availableQuantity()).thenReturn(5);
        OptionValue color = mock(OptionValue.class);
        OptionValue size = mock(OptionValue.class);
        when(color.getName()).thenReturn("검정");
        when(size.getName()).thenReturn("L");
        when(variant.getOptionValues()).thenReturn(List.of(color, size));
        givenSavedOrder(20L, 30L);

        // when
        useCase.execute(orderCommand(1));

        // then
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).saveAndFlush(orderCaptor.capture());
        assertEquals("검정, L", orderCaptor.getValue().getItems().get(0).getOptionCombination());
    }

    private CreateOrderCommand orderCommand(final int quantity) {
        return new CreateOrderCommand(BUYER_ID, List.of(new CreateOrderCommand.Item(VARIANT_ID, quantity)));
    }

    private void givenActiveBuyer() {
        when(userApi.existsActiveBuyer(BUYER_ID)).thenReturn(true);
    }

    private void givenOrderableItem(final int availableQuantity) {
        givenOrderableItem(VARIANT_ID, variant, product, inventory, "상품", UNIT_PRICE, availableQuantity);
    }

    private void givenOrderableItem(
            final Long variantId,
            final Variant requestedVariant,
            final Product requestedProduct,
            final Inventory requestedInventory,
            final String productName,
            final int unitPrice,
            final int availableQuantity
    ) {
        when(productRepository.findVariantForOrder(variantId)).thenReturn(Optional.of(requestedVariant));
        when(requestedVariant.getId()).thenReturn(variantId);
        when(requestedVariant.getProduct()).thenReturn(requestedProduct);
        when(requestedVariant.getOptionValues()).thenReturn(List.of());
        when(requestedVariant.isActive()).thenReturn(true);
        when(requestedVariant.getPrice()).thenReturn(unitPrice);
        when(requestedProduct.getStatus()).thenReturn(ProductStatus.ON_SALE);
        when(requestedProduct.getName()).thenReturn(productName);
        when(inventoryRepository.findByVariantId(variantId)).thenReturn(Optional.of(requestedInventory));
        when(requestedInventory.availableQuantity()).thenReturn(availableQuantity);
    }

    private void givenSavedOrder(final Long orderId, final Long orderItemId) {
        OrderItem savedItem = mock(OrderItem.class);
        when(savedOrder.getId()).thenReturn(orderId);
        when(savedItem.getId()).thenReturn(orderItemId);
        when(savedOrder.getItems()).thenReturn(List.of(savedItem));
        when(orderRepository.saveAndFlush(any(Order.class))).thenReturn(savedOrder);
    }
}
