package com.ecommerce.project.product.internal.persist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ecommerce.project.product.internal.domain.Product;
import com.ecommerce.project.product.internal.domain.ProductStatus;
import com.ecommerce.project.product.internal.domain.OptionValue;
import com.ecommerce.project.support.MySqlDataJpaTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@MySqlDataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("상품을 저장하고 판매자와 상품명으로 조회할 수 있다")
    void product_can_be_saved_and_found_by_seller_and_name() {
        // given
        Product product = Product.of(1L, "노트북 거치대", "알루미늄 거치대");

        // when
        Product savedProduct = productRepository.save(product);
        Product foundProduct = productRepository.findBySellerIdAndName(1L, "노트북 거치대").orElseThrow();

        // then
        assertNotNull(savedProduct.getId());
        assertEquals(savedProduct.getId(), foundProduct.getId());
        assertEquals(1L, foundProduct.getSellerId());
        assertEquals(ProductStatus.STOPPED, foundProduct.getStatus());
    }

    @Test
    @DisplayName("Variant 변경용 Product 조회 시 Option을 함께 조회할 수 있다")
    void product_can_be_found_for_change_with_options() {
        Product product = Product.of(1L, "변경상품", null);
        product.addOption("색상", "검정");
        Product savedProduct = productRepository.saveAndFlush(product);

        Product foundProduct = productRepository.findForChange(savedProduct.getId()).orElseThrow();

        assertEquals(savedProduct.getId(), foundProduct.getId());
        assertEquals(1, foundProduct.getOptions().size());
        assertEquals(1, foundProduct.getOptions().getFirst().getOptionValues().size());
    }

    @Test
    @DisplayName("Option이 없는 Product도 Variant 변경용으로 조회할 수 있다")
    void product_without_options_can_be_found_for_change() {
        // given
        Product product = Product.of(1L, "옵션없는상품", null);
        Product savedProduct = productRepository.saveAndFlush(product);

        // when
        Product foundProduct = productRepository.findForChange(savedProduct.getId()).orElseThrow();

        // then
        assertEquals(savedProduct.getId(), foundProduct.getId());
        assertEquals(0, foundProduct.getOptions().size());
    }

    @Test
    @DisplayName("조회된 Product에서 저장된 OptionValue ID를 찾을 수 있다")
    void found_product_can_find_saved_option_value() {
        // given
        Product product = Product.of(1L, "옵션상품", null);
        OptionValue optionValue = product.addOption("색상", "검정").getOptionValues().getFirst();
        Product savedProduct = productRepository.saveAndFlush(product);

        // when
        Product foundProduct = productRepository.findForChange(savedProduct.getId()).orElseThrow();
        List<OptionValue> foundOptionValues = foundProduct.findOptionValues(List.of(optionValue.getId()));

        // then
        assertEquals(1, foundOptionValues.size());
        assertEquals(optionValue.getId(), foundOptionValues.getFirst().getId());
    }
}
