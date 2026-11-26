package com.ecommerce.project.product.internal.persist;

import com.ecommerce.project.product.internal.domain.Product;
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
}
