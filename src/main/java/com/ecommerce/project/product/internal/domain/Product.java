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

    public Option addOption(final String name, final String... optionValueNames) {
        ensureOptionValueNamesExist(optionValueNames);
        ensureOptionCanBeAdded();
        ensureOptionNameNotDuplicated(name);
        Option option = Option.of(this, name);
        for (String optionValueName : optionValueNames) {
            option.addValue(optionValueName);
        }
        options.add(option);
        return option;
    }

    public OptionValue addOptionValue(final Option option, final String name) {
        ensureOptionBelongsToProduct(option);
        return option.addValue(name);
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

    public void changeName(final String name) {
        this.name = validateName(name);
    }

    public void changeDescription(final String description) {
        this.description = validateDescription(description);
    }

    public void changeOptionName(final Option option, final String name) {
        ensureOptionBelongsToProduct(option);
        ensureOptionNameNotDuplicated(option, name);
        option.changeName(name);
    }

    public void changeOptionValueName(final OptionValue optionValue, final String name) {
        Option option = optionValue == null ? null : optionValue.getOption();
        ensureOptionBelongsToProduct(option);
        ensureOptionValueNameNotDuplicated(optionValue, name);
        optionValue.changeName(name);
    }

    public void activateVariant(final Variant variant) {
        ensureVariantBelongsToProduct(variant);
        variant.activate();
    }

    public void stopVariant(final Variant variant) {
        ensureVariantBelongsToProduct(variant);
        variant.stop();
    }

    public void changeVariantPrice(final Variant variant, final int price) {
        ensureVariantBelongsToProduct(variant);
        variant.changePrice(price);
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
        if (options.stream().anyMatch(option -> option.getName().equalsIgnoreCase(name))) {
            throw new IllegalStateException("동일한 옵션명은 등록할 수 없습니다.");
        }
    }

    private void ensureOptionNameNotDuplicated(final Option target, final String name) {
        if (options.stream()
                .filter(option -> option != target)
                .anyMatch(option -> option.getName().equalsIgnoreCase(name))) {
            throw new IllegalStateException("동일한 옵션명은 등록할 수 없습니다.");
        }
    }

    private void ensureOptionValueNameNotDuplicated(final OptionValue target, final String name) {
        if (target.getOption().getOptionValues().stream()
                .filter(optionValue -> optionValue != target)
                .anyMatch(optionValue -> optionValue.getName().equalsIgnoreCase(name))) {
            throw new IllegalStateException("동일한 옵션값은 등록할 수 없습니다.");
        }
    }

    private void ensureOptionCanBeAdded() {
        if (!variants.isEmpty()) {
            throw new IllegalStateException("Variant가 있는 Product에는 새로운 옵션을 추가할 수 없습니다.");
        }
    }

    private void ensureOptionValueNamesExist(final String[] optionValueNames) {
        if (optionValueNames == null || optionValueNames.length == 0) {
            throw new IllegalArgumentException("Option은 하나 이상의 OptionValue를 가져야 합니다.");
        }
    }

    private void ensureOptionBelongsToProduct(final Option option) {
        if (option == null || !options.contains(option)) {
            throw new IllegalArgumentException("Option은 Product에 속해야 합니다.");
        }
    }

    private void ensureVariantBelongsToProduct(final Variant variant) {
        if (variant == null || !variants.contains(variant)) {
            throw new IllegalArgumentException("Variant는 Product에 속해야 합니다.");
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
        if (status == this.status) {
            throw new IllegalStateException("Product 상태는 ON_SALE과 STOPPED 사이에서만 전환할 수 있습니다.");
        }
    }

    private String validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("상품명은 3자 이상 12자 이하이어야 합니다.");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() < 3 || trimmedName.length() > 12) {
            throw new IllegalArgumentException("상품명은 3자 이상 12자 이하이어야 합니다.");
        }
        return trimmedName;
    }

    private String validateDescription(final String description) {
        if (description == null) {
            return null;
        }
        if (description.isBlank()) {
            throw new IllegalArgumentException("상품 설명은 비어 있거나 공백일 수 없습니다.");
        }
        String trimmedDescription = description.trim();
        if (trimmedDescription.length() > 100) {
            throw new IllegalArgumentException("상품 설명은 100자 이하이어야 합니다.");
        }
        return trimmedDescription;
    }
}
