package com.jpos.sale.service.implementation;

import com.jpos.sale.exception.ProductNotFoundException;
import com.jpos.sale.service.ProductCostProvider;
import com.jpos.inventory.service.InventoryService;

import java.util.UUID;

/**
 * Adapter that provides product costs from the inventory module to the sales module.
 * Converts inventory exceptions to sales module exceptions to maintain clean module boundaries.
 */
public class InventoryCostProvider implements ProductCostProvider {
    private final InventoryService inventoryService;

    public InventoryCostProvider(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public double getProductCost(UUID productId) {
        try {
            return inventoryService.getProductCostById(productId);
        } catch (com.jpos.inventory.exception.ProductNotFoundException e) {
            // Convert inventory exception to sale module exception
            throw new ProductNotFoundException(productId);
        }
    }
}
