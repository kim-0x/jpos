package com.jos.inventory.service;

import com.jos.inventory.model.ProductInventoryRecord;

public interface InventoryService {
    abstract void entryStock(String barcode, String name, String category, double cost, float numberInStock);
    abstract ProductInventoryRecord[] getProductsWithLatestCost();
}
