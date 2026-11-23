package com.ecommerce.project.product.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "variant")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Variant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(name = "variant_status", nullable = false)
    private VariantStatus status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "variant_option_value",
            joinColumns = @JoinColumn(name = "variant_id"),
            inverseJoinColumns = @JoinColumn(name = "option_value_id")
    )
    @Getter(AccessLevel.NONE)
    private List<OptionValue> optionValues = new ArrayList<>();

    private Variant(final Product product, final int price, final List<OptionValue> optionValues) {
        this.product = validateProduct(product);
        this.price = validatePrice(price);
        this.optionValues = new ArrayList<>(optionValues);
        this.status = VariantStatus.STOPPED;
    }

    public void activate() {
        status = VariantStatus.ACTIVE;
    }

    public void stop() {
        status = VariantStatus.STOPPED;
    }

    static Variant of(final Product product, final int price, final List<OptionValue> optionValues) {
        return new Variant(product, price, optionValues);
    }

    boolean hasSameOptionValues(final List<OptionValue> values) {
        return optionValues.equals(values);
    }

    boolean isActive() {
        return status == VariantStatus.ACTIVE;
    }

    public List<OptionValue> getOptionValues() {
        return List.copyOf(optionValues);
    }

    // 불변식 =====================================

    private Product validateProduct(final Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Variant에는 Product가 필요합니다.");
        }
        return product;
    }

    private int validatePrice(final int price) {
        if (price < 0) {
            throw new IllegalArgumentException("Variant 가격은 0 이상이어야 합니다.");
        }
        return price;
    }
}
