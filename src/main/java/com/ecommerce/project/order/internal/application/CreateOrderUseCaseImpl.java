package com.ecommerce.project.order.internal.application;

import com.ecommerce.project.inventory.internal.domain.Inventory;
import com.ecommerce.project.inventory.internal.persist.InventoryRepository;
import com.ecommerce.project.order.internal.application.command.CreateOrderCommand;
import com.ecommerce.project.order.internal.application.result.CreateOrderFailureResult;
import com.ecommerce.project.order.internal.application.result.CreateOrderResult;
import com.ecommerce.project.order.internal.application.result.CreateOrderSuccessResult;
import com.ecommerce.project.order.internal.application.usecase.CreateOrderUseCase;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCaseImpl implements CreateOrderUseCase {
    private static final int ORDER_EXPIRATION_MINUTES = 10;

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final UserApi userApi;
    private final Clock clock;

    public CreateOrderUseCaseImpl(
            final ProductRepository productRepository,
            final InventoryRepository inventoryRepository,
            final OrderRepository orderRepository,
            final UserApi userApi
    ) {
        this(productRepository, inventoryRepository, orderRepository, userApi, Clock.systemUTC());
    }

    @Override
    @Transactional
    public CreateOrderResult execute(final CreateOrderCommand command) {
        validateCommand(command);
        ensureBuyerCanCreateOrder(command.buyerId());

        OrderPreparation preparation = prepareOrder(command.items());
        if (preparation.hasShortages()) {
            return new CreateOrderFailureResult(preparation.shortages());
        }

        Order savedOrder = saveOrder(command.buyerId(), preparation.orderItems());
        reserveInventory(preparation.items(), savedOrder);
        return new CreateOrderSuccessResult(savedOrder.getId());
    }

    private void validateCommand(final CreateOrderCommand command) {
        if (command == null || command.buyerId() == null) {
            throw new IllegalArgumentException("구매자와 주문 상품은 필수입니다.");
        }
        if (command.items() == null || command.items().isEmpty()) {
            throw new IllegalArgumentException("주문 상품은 하나 이상 필요합니다.");
        }
    }

    private void ensureBuyerCanCreateOrder(final Long buyerId) {
        if (!userApi.existsActiveBuyer(buyerId)) {
            throw new IllegalArgumentException("구매자 사용자를 찾을 수 없습니다.");
        }
    }

    private OrderPreparation prepareOrder(final List<CreateOrderCommand.Item> requests) {
        List<PreparedItem> preparedItems = new ArrayList<>();
        List<CreateOrderFailureResult.Shortage> shortages = new ArrayList<>();

        for (CreateOrderCommand.Item request : requests) {
            PreparedItem preparedItem = prepareItem(request);
            preparedItems.add(preparedItem);
            if (preparedItem.shortage() != null) {
                shortages.add(preparedItem.shortage());
            }
        }
        return new OrderPreparation(preparedItems, shortages);
    }

    private PreparedItem prepareItem(final CreateOrderCommand.Item request) {
        validateItemRequest(request);

        Variant variant = findVariant(request.variantId());
        Product product = variant.getProduct();
        ensurePurchasable(variant, product);

        Inventory inventory = inventoryRepository.findByVariantId(request.variantId())
                .orElseThrow(() -> new IllegalStateException("재고를 찾을 수 없습니다."));
        int availableQuantity = inventory.availableQuantity();
        CreateOrderFailureResult.Shortage shortage = createShortageIfNeeded(
                request, product, availableQuantity
        );
        OrderItem orderItem = OrderItem.of(
                variant.getId(),
                product.getName(),
                optionCombination(variant),
                variant.getPrice(),
                request.quantity()
        );
        return new PreparedItem(orderItem, inventory, request.quantity(), shortage);
    }

    private void validateItemRequest(final CreateOrderCommand.Item request) {
        if (request == null || request.variantId() == null) {
            throw new IllegalArgumentException("주문 상품과 Variant ID는 필수입니다.");
        }
    }

    private Variant findVariant(final Long variantId) {
        return productRepository.findVariantForOrder(variantId)
                .orElseThrow(() -> new IllegalArgumentException("상품 옵션을 찾을 수 없습니다."));
    }

    private void ensurePurchasable(final Variant variant, final Product product) {
        if (product.getStatus() != ProductStatus.ON_SALE || !variant.isActive()) {
            throw new IllegalStateException("판매 중인 상품 옵션만 주문할 수 있습니다.");
        }
    }

    private CreateOrderFailureResult.Shortage createShortageIfNeeded(
            final CreateOrderCommand.Item request,
            final Product product,
            final int availableQuantity
    ) {
        if (request.quantity() <= availableQuantity) {
            return null;
        }
        return new CreateOrderFailureResult.Shortage(
                request.variantId(),
                product.getName(),
                request.quantity(),
                availableQuantity
        );
    }

    private Order saveOrder(final Long buyerId, final List<OrderItem> orderItems) {
        LocalDateTime createdAt = LocalDateTime.now(clock);
        Order order = Order.of(
                UUID.randomUUID(),
                buyerId,
                orderItems,
                createdAt,
                createdAt.plusMinutes(ORDER_EXPIRATION_MINUTES)
        );
        return orderRepository.saveAndFlush(order);
    }

    private void reserveInventory(final List<PreparedItem> items, final Order savedOrder) {
        List<OrderItem> savedOrderItems = savedOrder.getItems();
        for (int index = 0; index < items.size(); index++) {
            PreparedItem item = items.get(index);
            item.inventory().reserve(
                    savedOrder.getId(),
                    savedOrderItems.get(index).getId(),
                    item.quantity()
            );
        }
    }

    private String optionCombination(final Variant variant) {
        return variant.getOptionValues().stream()
                .map(OptionValue::getName)
                .reduce((left, right) -> left + ", " + right)
                .orElse(null);
    }

    private record PreparedItem(
            OrderItem orderItem,
            Inventory inventory,
            int quantity,
            CreateOrderFailureResult.Shortage shortage
    ) {
    }

    private record OrderPreparation(
            List<PreparedItem> items,
            List<CreateOrderFailureResult.Shortage> shortages
    ) {
        private OrderPreparation {
            items = List.copyOf(items);
            shortages = List.copyOf(shortages);
        }

        private List<OrderItem> orderItems() {
            return items.stream().map(PreparedItem::orderItem).toList();
        }

        private boolean hasShortages() {
            return !shortages.isEmpty();
        }
    }
}
