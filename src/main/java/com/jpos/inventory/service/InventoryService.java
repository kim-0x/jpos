package com.jpos.inventory.service;

import com.jpos.inventory.model.StockRecord;

import java.util.UUID;

public interface InventoryService {
    abstract void entryStock(String barcode, double cost, float numberInStock);
    abstract StockRecord[] getStockReport();
    abstract double getProductCostById(UUID productId);
}
