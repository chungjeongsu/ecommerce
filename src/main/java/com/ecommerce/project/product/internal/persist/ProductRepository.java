package com.ecommerce.project.product.internal.persist;

import com.ecommerce.project.product.internal.domain.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySellerIdAndName(Long sellerId, String name);
}
