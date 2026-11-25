package com.ecommerce.project.product.internal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OptionValueTest {

    @Test
    @DisplayName("옵션값은 옵션을 통해 생성되고 이름을 가진다")
    void option_value_is_created_with_option() {
        // given
        Option option = Product.of(1L, "노트북 거치대", null).addOption("색상", "실버");

        // when
        OptionValue optionValue = option.getOptionValues().get(0);

        // then
        assertEquals(option, optionValue.getOption());
        assertEquals("실버", optionValue.getName());
    }

    @Test
    @DisplayName("옵션값 이름은 1자 이상 10자 이하이어야 한다")
    void option_value_name_must_have_valid_length() {
        // given
        Option option = Product.of(1L, "노트북 거치대", null).addOption("색상", "실버");

        // when & then
        assertThrows(IllegalArgumentException.class, () -> Product.of(1L, "노트북 거치대", null).addOption("색상", ""));
        assertThrows(IllegalArgumentException.class, () -> Product.of(1L, "노트북 거치대", null).addOption("색상", "12345678901"));
        assertThrows(IllegalArgumentException.class, () -> Product.of(1L, "노트북 거치대", null).addOption("색상", "   "));
    }
}
