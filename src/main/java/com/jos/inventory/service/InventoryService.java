package com.jos.inventory.service;

import com.jos.inventory.model.StockRecord;

public interface InventoryService {
    abstract void entryStock(String barcode, double cost, float numberInStock);
    abstract StockRecord[] getStockReport();
}
