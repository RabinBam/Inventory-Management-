package org.inventory;

import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    // You will add database repository calls here later
    public double getTotalStockValue() {
        return 450000.00; // Hardcoded for now
    }
}