package com.jos.inventory.repository;

import com.jos.inventory.model.ProductQuery;
import com.jos.inventory.model.StockItem;

public interface InventoryRepository {
    abstract void stockOut(StockItem stockItem);
    abstract void stockIn(StockItem stockItem);
    abstract StockItem[] getStockItems();
    abstract double getProductCost(ProductQuery productQuery);
    abstract float getStockLevelOf(ProductQuery productQuery);
}
