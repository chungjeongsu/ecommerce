package com.ecommerce.project.product.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "option_value")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OptionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_value_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private Option option;

    @Column(nullable = false, length = 10)
    private String name;

    private OptionValue(final Option option, final String name) {
        this.option = validateOption(option);
        this.name = validateName(name);
    }

    static OptionValue of(final Option option, final String name) {
        return new OptionValue(option, name);
    }

    void changeName(final String name) {
        this.name = validateName(name);
    }

    // 불변식 =====================================

    private Option validateOption(final Option option) {
        if (option == null) {
            throw new IllegalArgumentException("OptionValue에는 Option이 필요합니다.");
        }
        return option;
    }

    private String validateName(final String name) {
        if (name == null || name.isBlank() || name.length() > 10) {
            throw new IllegalArgumentException("옵션값은 1자 이상 10자 이하이어야 합니다.");
        }
        return name;
    }
}
