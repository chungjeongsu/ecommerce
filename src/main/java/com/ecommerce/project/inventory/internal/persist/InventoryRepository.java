package com.ecommerce.project.inventory.internal.persist;

import com.ecommerce.project.inventory.internal.domain.Inventory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByVariantId(Long variantId);
}
