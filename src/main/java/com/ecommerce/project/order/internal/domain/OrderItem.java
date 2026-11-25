package com.ecommerce.project.order.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "option_combination", length = 1000)
    private String optionCombination;

    @Column(name = "unit_price", nullable = false)
    private long unitPrice;

    @Column(nullable = false)
    private int quantity;

    private OrderItem(
            final Long variantId,
            final String productName,
            final String optionCombination,
            final long unitPrice,
            final int quantity
    ) {
        this.variantId = validateVariantId(variantId);
        this.productName = validateProductName(productName);
        this.optionCombination = optionCombination;
        this.unitPrice = validateUnitPrice(unitPrice);
        this.quantity = validateQuantity(quantity);
        validateAmount(this.unitPrice, this.quantity);
    }

    public long getAmount() {
        return Math.multiplyExact(unitPrice, quantity);
    }

    public static OrderItem of(
            final Long variantId,
            final String productName,
            final String optionCombination,
            final long unitPrice,
            final int quantity
    ) {
        return new OrderItem(variantId, productName, optionCombination, unitPrice, quantity);
    }

    // 불변식 =====================================

    private Long validateVariantId(final Long variantId) {
        if (variantId == null) {
            throw new IllegalArgumentException("Variant ID는 필수입니다.");
        }
        return variantId;
    }

    private String validateProductName(final String productName) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("주문 당시 상품명은 필수입니다.");
        }
        return productName;
    }

    private long validateUnitPrice(final long unitPrice) {
        if (unitPrice < 0) {
            throw new IllegalArgumentException("주문 당시 단가는 0 이상이어야 합니다.");
        }
        return unitPrice;
    }

    private int validateQuantity(final int quantity) {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("주문 수량은 1개 이상 100개 이하여야 합니다.");
        }
        return quantity;
    }

    private void validateAmount(final long unitPrice, final int quantity) {
        try {
            Math.multiplyExact(unitPrice, quantity);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("주문 항목 금액이 허용 범위를 초과했습니다.", exception);
        }
    }

}
