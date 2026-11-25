package com.ecommerce.project.product.internal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    @DisplayName("상품은 생성 시 판매 중지 상태로 시작한다")
    void product_starts_stopped() {
        // given & when
        Product product = Product.of(1L, "노트북 거치대", "알루미늄 거치대");

        // then
        assertEquals(ProductStatus.STOPPED, product.getStatus());
    }

    @Test
    @DisplayName("상품명은 3자 이상 12자 이하이어야 한다")
    void product_name_must_have_valid_length() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> Product.of(1L, "ab", null));
        assertThrows(IllegalArgumentException.class, () -> Product.of(1L, "1234567890123", null));
        assertThrows(IllegalArgumentException.class, () -> Product.of(1L, "   ", null));

        Product product = Product.of(1L, " 노트북 거치대 ", " 알루미늄 거치대 ");
        assertEquals("노트북 거치대", product.getName());
        assertEquals("알루미늄 거치대", product.getDescription());
    }

    @Test
    @DisplayName("판매자 ID는 필수이다")
    void seller_id_is_required() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> Product.of(null, "노트북 거치대", null));
    }

    @Test
    @DisplayName("상품 설명은 100자 이하이어야 한다")
    void product_description_must_not_exceed_100_characters() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> Product.of(1L, "노트북 거치대", "a".repeat(101)));
        assertDoesNotThrow(() -> Product.of(1L, "노트북 거치대", "a".repeat(100)));
        assertThrows(IllegalArgumentException.class, () -> Product.of(1L, "노트북 거치대", "   "));
    }

    @Test
    @DisplayName("상품은 동일한 옵션명을 중복 등록할 수 없다")
    void product_cannot_have_duplicate_option_names() {
        // given
        Product product = Product.of(1L, "노트북 거치대", null);
        product.addOption("Color", "Red");

        // when & then
        assertThrows(IllegalStateException.class, () -> product.addOption("color", "Blue"));
    }

    @Test
    @DisplayName("상품 옵션은 동일한 옵션값을 중복 등록할 수 없다")
    void option_cannot_have_duplicate_values() {
        // given
        Option option = Product.of(1L, "노트북 거치대", null).addOption("색상", "Silver");

        // when & then
        assertThrows(IllegalStateException.class, () -> option.addValue("silver"));
    }

    @Test
    @DisplayName("Variant 가격은 음수일 수 없다")
    void variant_price_cannot_be_negative() {
        // given
        Product product = Product.of(1L, "노트북 거치대", null);
        OptionValue optionValue = product.addOption("색상", "실버").getOptionValues().get(0);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> product.addVariant(-1, List.of(optionValue)));
    }

    @Test
    @DisplayName("Variant는 다른 상품의 옵션값을 사용할 수 없다")
    void variant_cannot_use_value_from_another_product() {
        // given
        Product product = Product.of(1L, "노트북 거치대", null);
        Product anotherProduct = Product.of(2L, "무선 마우스", null);
        OptionValue foreignValue = anotherProduct.addOption("색상", "검정").getOptionValues().get(0);
        product.addOption("색상", "실버");

        // when & then
        assertThrows(IllegalArgumentException.class, () -> product.addVariant(10000, List.of(foreignValue)));
    }

    @Test
    @DisplayName("Variant는 하나의 옵션에서 여러 옵션값을 선택할 수 없다")
    void variant_cannot_use_multiple_values_from_same_option() {
        // given
        Product product = Product.of(1L, "노트북 거치대", null);
        Option option = product.addOption("색상", "실버");
        OptionValue silver = option.getOptionValues().get(0);
        OptionValue black = option.addValue("검정");

        // when & then
        assertThrows(IllegalArgumentException.class, () -> product.addVariant(10000, List.of(silver, black)));
    }

    @Test
    @DisplayName("동일한 옵션 조합의 Variant는 중복 등록할 수 없다")
    void variant_option_combination_cannot_be_duplicated() {
        // given
        Product product = Product.of(1L, "노트북 거치대", null);
        OptionValue optionValue = product.addOption("색상", "실버").getOptionValues().get(0);
        product.addVariant(10000, List.of(optionValue));

        // when & then
        assertThrows(IllegalStateException.class, () -> product.addVariant(12000, List.of(optionValue)));
    }

    @Test
    @DisplayName("판매 가능한 Variant가 있어야 상품을 판매 상태로 변경할 수 있다")
    void product_can_be_on_sale_only_with_active_variant() {
        // given
        Product product = Product.of(1L, "노트북 거치대", null);
        Option option = product.addOption("색상", "실버");
        OptionValue optionValue = option.getOptionValues().get(0);

        // when & then
        assertThrows(IllegalStateException.class, () -> product.changeStatus(ProductStatus.ON_SALE));

        Variant variant = product.addVariant(10000, List.of(optionValue));
        product.activateVariant(variant);
        product.changeStatus(ProductStatus.ON_SALE);

        assertEquals(ProductStatus.ON_SALE, product.getStatus());
    }

    @Test
    @DisplayName("상품은 빈 Option을 생성할 수 없다")
    void product_cannot_create_empty_option() {
        Product product = Product.of(1L, "노트북 거치대", null);

        assertThrows(IllegalArgumentException.class, () -> product.addOption("색상"));
    }

    @Test
    @DisplayName("상품의 옵션 컬렉션은 외부에서 변경할 수 없다")
    void product_options_are_unmodifiable() {
        // given
        Product product = Product.of(1L, "노트북 거치대", null);
        product.addOption("색상", "실버");

        // when & then
        assertThrows(UnsupportedOperationException.class, () -> product.getOptions().clear());
    }

    @Test
    @DisplayName("상품의 Variant 컬렉션은 외부에서 변경할 수 없다")
    void product_variants_are_unmodifiable() {
        Product product = Product.of(1L, "노트북 거치대", null);
        OptionValue optionValue = product.addOption("색상", "실버").getOptionValues().get(0);
        product.addVariant(10000, List.of(optionValue));

        assertThrows(UnsupportedOperationException.class, () -> product.getVariants().clear());
    }

    @Test
    @DisplayName("Variant가 있으면 새로운 Option을 추가할 수 없다")
    void product_cannot_add_option_after_variant_exists() {
        Product product = Product.of(1L, "노트북 거치대", null);
        Option color = product.addOption("색상", "실버");
        product.addVariant(10000, List.of(color.getOptionValues().get(0)));

        assertThrows(IllegalStateException.class, () -> product.addOption("사이즈", "중간"));
    }

    @Test
    @DisplayName("Variant 조합은 OptionValue 입력 순서가 달라도 중복 등록할 수 없다")
    void variant_option_combination_is_order_independent() {
        Product product = Product.of(1L, "노트북 거치대", null);
        Option color = product.addOption("색상", "실버");
        Option size = product.addOption("사이즈", "중간");
        OptionValue silver = color.getOptionValues().get(0);
        OptionValue medium = size.getOptionValues().get(0);

        product.addVariant(10000, List.of(silver, medium));

        assertThrows(IllegalStateException.class, () -> product.addVariant(12000, List.of(medium, silver)));
    }

    @Test
    @DisplayName("Product Root는 Variant를 활성화하고 중지한다")
    void product_manages_variant_status() {
        Product product = Product.of(1L, "노트북 거치대", null);
        OptionValue optionValue = product.addOption("색상", "실버").getOptionValues().get(0);
        Variant variant = product.addVariant(10000, List.of(optionValue));

        product.activateVariant(variant);
        assertEquals(VariantStatus.ACTIVE, variant.getStatus());

        product.stopVariant(variant);
        assertEquals(VariantStatus.STOPPED, variant.getStatus());
    }

    @Test
    @DisplayName("Product Root는 Variant 가격을 변경한다")
    void product_changes_variant_price() {
        Product product = Product.of(1L, "노트북 거치대", null);
        OptionValue optionValue = product.addOption("색상", "실버").getOptionValues().get(0);
        Variant variant = product.addVariant(10000, List.of(optionValue));

        product.changeVariantPrice(variant, 12000);

        assertEquals(12000, variant.getPrice());
    }

    @Test
    @DisplayName("Product와 Option 계층의 이름 변경은 각 불변식을 지킨다")
    void product_changes_names_with_invariants() {
        Product product = Product.of(1L, "노트북 거치대", null);
        Option option = product.addOption("색상", "실버");
        OptionValue optionValue = option.getOptionValues().get(0);

        product.changeName(" 새 상품 ");
        product.changeDescription(" 새 설명 ");
        product.changeOptionName(option, "Color");
        product.changeOptionValueName(optionValue, "Silver");

        assertEquals("새 상품", product.getName());
        assertEquals("새 설명", product.getDescription());
        assertEquals("Color", option.getName());
        assertEquals("Silver", optionValue.getName());
    }
}
