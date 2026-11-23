package com.ecommerce.project.product.internal.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "product_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 10)
    private String name;

    @OneToMany(
            mappedBy = "option",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Getter(AccessLevel.NONE)
    private List<OptionValue> optionValues = new ArrayList<>();

    private Option(final Product product, final String name) {
        this.product = validateProduct(product);
        this.name = validateName(name);
    }

    public OptionValue addValue(final String name) {
        ensureOptionValueNameNotDuplicated(name);

        OptionValue optionValue = OptionValue.of(this, name);
        optionValues.add(optionValue);
        return optionValue;
    }

    static Option of(final Product product, final String name) {
        return new Option(product, name);
    }

    public List<OptionValue> getOptionValues() {
        return List.copyOf(optionValues);
    }

    // 불변식 =====================================

    private Product validateProduct(final Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Option에는 Product가 필요합니다.");
        }
        return product;
    }

    private String validateName(final String name) {
        if (name == null || name.isBlank() || name.length() > 10) {
            throw new IllegalArgumentException("옵션명은 1자 이상 10자 이하이어야 합니다.");
        }
        return name;
    }

    private void ensureOptionValueNameNotDuplicated(final String name) {
        if (optionValues.stream().anyMatch(value -> value.getName().equals(name))) {
            throw new IllegalStateException("동일한 옵션값은 등록할 수 없습니다.");
        }
    }
}
