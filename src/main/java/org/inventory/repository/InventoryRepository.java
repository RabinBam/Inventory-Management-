package org.inventory.repository;

import org.inventory.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    // Required for Binary Search: List must be sorted by name
    List<InventoryItem> findAllByOrderByNameAsc();
}