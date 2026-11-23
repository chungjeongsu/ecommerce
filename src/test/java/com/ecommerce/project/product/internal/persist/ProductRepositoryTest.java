package com.ecommerce.project.product.internal.persist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ecommerce.project.product.internal.domain.Product;
import com.ecommerce.project.product.internal.domain.ProductStatus;
import com.ecommerce.project.support.MySqlDataJpaTest;
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
}
