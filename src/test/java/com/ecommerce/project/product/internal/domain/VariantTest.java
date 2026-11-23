package com.ecommerce.project.product.internal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VariantTest {

    @Test
    @DisplayName("Variant는 생성 시 판매 중지 상태로 시작한다")
    void variant_starts_stopped() {
        // given
        Product product = Product.of(1L, "노트북 거치대", null);
        OptionValue optionValue = product.addOption("색상").addValue("실버");

        // when
        Variant variant = product.addVariant(10000, List.of(optionValue));

        // then
        assertEquals(VariantStatus.STOPPED, variant.getStatus());
        assertEquals(10000, variant.getPrice());
    }

    @Test
    @DisplayName("Variant의 옵션값 컬렉션은 외부에서 변경할 수 없다")
    void variant_option_values_are_unmodifiable() {
        // given
        Product product = Product.of(1L, "노트북 거치대", null);
        OptionValue optionValue = product.addOption("색상").addValue("실버");
        Variant variant = product.addVariant(10000, List.of(optionValue));

        // when & then
        assertThrows(UnsupportedOperationException.class, () -> variant.getOptionValues().clear());
    }

    @Test
    @DisplayName("Variant는 ACTIVE와 STOPPED 사이로만 상태를 변경할 수 있다")
    void variant_can_change_between_active_and_stopped() {
        // given
        Product product = Product.of(1L, "노트북 거치대", null);
        OptionValue optionValue = product.addOption("색상").addValue("실버");
        Variant variant = product.addVariant(10000, List.of(optionValue));

        // when
        variant.activate();

        // then
        assertEquals(VariantStatus.ACTIVE, variant.getStatus());

        // when
        variant.stop();

        // then
        assertEquals(VariantStatus.STOPPED, variant.getStatus());
    }

    @Test
    @DisplayName("Variant 가격은 0 이상이어야 한다")
    void variant_price_must_not_be_negative() {
        // given
        Product product = Product.of(1L, "노트북 거치대", null);
        OptionValue optionValue = product.addOption("색상").addValue("실버");

        // when & then
        assertThrows(IllegalArgumentException.class, () -> product.addVariant(-1, List.of(optionValue)));
    }
}
