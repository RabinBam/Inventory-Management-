package org.inventory.service;

import org.inventory.model.InventoryItem;
import org.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InventoryService {
    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    // Fixes the "Cannot resolve method" error
    public InventoryItem searchByName(String targetName) {
        List<InventoryItem> items = repository.findAllByOrderByNameAsc();
        int low = 0;
        int high = items.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int comparison = items.get(mid).getName().compareToIgnoreCase(targetName);

            if (comparison == 0) return items.get(mid);
            if (comparison < 0) low = mid + 1;
            else high = mid - 1;
        }
        return null;
    }

    public double getTotalStockValue() {
        return repository.findAll().stream()
                .mapToDouble(item -> item.getPrice() * item.getStock())
                .sum();
    }
}