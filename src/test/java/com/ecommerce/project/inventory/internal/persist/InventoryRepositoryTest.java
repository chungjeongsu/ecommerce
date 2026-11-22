package com.ecommerce.project.inventory.internal.persist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.project.inventory.internal.domain.Inventory;
import com.ecommerce.project.inventory.internal.domain.InventoryStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    @DisplayName("Inventory를 저장하고 Variant ID로 조회할 수 있어야 한다")
    void inventory_can_be_saved_and_found_by_variant_id() {
        // given
        Inventory inventory = Inventory.of(1L, 10);

        // when
        Inventory savedInventory = inventoryRepository.save(inventory);
        Inventory foundInventory = inventoryRepository.findByVariantId(1L).orElseThrow();

        // then
        assertTrue(savedInventory.getId() != null);
        assertEquals(savedInventory.getId(), foundInventory.getId());
        assertEquals(1L, foundInventory.getVariantId());
        assertEquals(10, foundInventory.getTotalQuantity());
        assertEquals(InventoryStatus.ACTIVE, foundInventory.getStatus());
    }
}
