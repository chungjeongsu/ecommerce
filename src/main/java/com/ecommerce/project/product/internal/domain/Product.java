package com.ecommerce.project.product.internal.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(nullable = false, length = 12)
    private String name;

    @Column(length = 100)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private List<Option> options = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private List<Variant> variants = new ArrayList<>();

    private Product(final Long sellerId, final String name, final String description) {
        this.sellerId = sellerId;
        this.name = validateName(name);
        this.description = validateDescription(description);
        this.status = ProductStatus.STOPPED;
    }

    public Option addOption(final String name) {
        ensureOptionNameNotDuplicated(name);
        Option option = Option.of(this, name);
        options.add(option);
        return option;
    }

    public Variant addVariant(final int price, final List<OptionValue> optionValues) {
        ensureVariantOptionValuesValid(optionValues);
        ensureVariantOptionCombinationNotDuplicated(optionValues);
        Variant variant = Variant.of(this, price, optionValues);
        variants.add(variant);
        return variant;
    }

    public void changeStatus(final ProductStatus status) {
        ensureStatusChangeAllowed(status);
        this.status = status;
    }

    public List<Option> getOptions() {
        return List.copyOf(options);
    }

    public List<Variant> getVariants() {
        return List.copyOf(variants);
    }

    public static Product of(final Long sellerId, final String name, final String description) {
        ensureSellerExists(sellerId);
        return new Product(sellerId, name, description);
    }

    // 불변식 =====================================

    private static void ensureSellerExists(final Long sellerId) {
        if (sellerId == null) {
            throw new IllegalArgumentException("판매자 ID는 필수입니다.");
        }
    }

    private void ensureOptionNameNotDuplicated(final String name) {
        if (options.stream().anyMatch(option -> option.getName().equals(name))) {
            throw new IllegalStateException("동일한 옵션명은 등록할 수 없습니다.");
        }
    }

    private void ensureVariantOptionValuesValid(final List<OptionValue> optionValues) {
        if (optionValues == null || optionValues.size() != options.size()) {
            throw new IllegalArgumentException("Variant는 모든 Product 옵션의 값을 포함해야 합니다.");
        }
        if (optionValues.stream().anyMatch(optionValue ->
                optionValue == null || !options.contains(optionValue.getOption()))) {
            throw new IllegalArgumentException("Variant의 옵션값은 Product에 속한 옵션값이어야 합니다.");
        }
        if (optionValues.stream().map(OptionValue::getOption).distinct().count() != optionValues.size()) {
            throw new IllegalArgumentException("Variant는 하나의 옵션에서 하나의 옵션값만 포함해야 합니다.");
        }
    }

    private void ensureVariantOptionCombinationNotDuplicated(final List<OptionValue> optionValues) {
        if (variants.stream().anyMatch(variant -> variant.hasSameOptionValues(optionValues))) {
            throw new IllegalStateException("동일한 옵션 조합의 Variant는 중복 등록할 수 없습니다.");
        }
    }

    private void ensureStatusChangeAllowed(final ProductStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("상품 상태는 필수입니다.");
        }
        if (status == ProductStatus.ON_SALE && variants.stream().noneMatch(Variant::isActive)) {
            throw new IllegalStateException("판매 가능한 Variant가 있어야 상품을 판매할 수 있습니다.");
        }
        if (status == ProductStatus.ON_SALE && options.stream().anyMatch(option -> option.getOptionValues().isEmpty())) {
            throw new IllegalStateException("모든 Product 옵션은 하나 이상의 옵션값을 가져야 합니다.");
        }
    }

    private String validateName(final String name) {
        if (name == null || name.isBlank() || name.length() < 3 || name.length() > 12) {
            throw new IllegalArgumentException("상품명은 3자 이상 12자 이하이어야 합니다.");
        }
        return name;
    }

    private String validateDescription(final String description) {
        if (description != null && description.length() > 100) {
            throw new IllegalArgumentException("상품 설명은 100자 이하이어야 합니다.");
        }
        return description;
    }
}
