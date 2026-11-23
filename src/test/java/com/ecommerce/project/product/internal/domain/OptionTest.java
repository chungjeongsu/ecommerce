package com.ecommerce.project.product.internal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OptionTest {

    @Test
    @DisplayName("옵션은 Product를 통해 생성되고 Product에 연결된다")
    void option_is_created_with_product() {
        // given & when
        Product product = Product.of(1L, "노트북 거치대", null);
        Option option = product.addOption("색상");

        // then
        assertEquals(product, option.getProduct());
        assertEquals("색상", option.getName());
    }

    @Test
    @DisplayName("옵션명은 1자 이상 10자 이하이어야 한다")
    void option_name_must_have_valid_length() {
        // given
        Product product = Product.of(1L, "노트북 거치대", null);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> product.addOption(""));
        assertThrows(IllegalArgumentException.class, () -> product.addOption("12345678901"));
        assertThrows(IllegalArgumentException.class, () -> product.addOption("   "));
    }

    @Test
    @DisplayName("옵션은 옵션값을 추가하고 옵션과의 관계를 유지한다")
    void option_adds_value() {
        // given
        Option option = Product.of(1L, "노트북 거치대", null).addOption("색상");

        // when
        OptionValue optionValue = option.addValue("실버");

        // then
        assertEquals(option, optionValue.getOption());
        assertEquals(1, option.getOptionValues().size());
    }

    @Test
    @DisplayName("옵션의 옵션값 컬렉션은 외부에서 변경할 수 없다")
    void option_values_are_unmodifiable() {
        // given
        Option option = Product.of(1L, "노트북 거치대", null).addOption("색상");
        option.addValue("실버");

        // when & then
        assertThrows(UnsupportedOperationException.class, () -> option.getOptionValues().clear());
    }
}
