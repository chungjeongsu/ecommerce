package com.ecommerce.project.product.internal.persist;

import com.ecommerce.project.product.internal.domain.Product;
import com.ecommerce.project.product.internal.domain.Variant;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySellerIdAndName(Long sellerId, String name);

    @Query("""
               select distinct product
               from Product product
               left join fetch product.options options
               where product.id = :productId
               """)
    Optional<Product> findForChange(@Param("productId") Long productId);

    @Query("""
            select distinct variant
            from Variant variant
            join fetch variant.product product
            left join fetch variant.optionValues
            where variant.id = :variantId
            """)
    Optional<Variant> findVariantForOrder(@Param("variantId") Long variantId);
}
