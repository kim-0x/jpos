package com.jpos.inventory.repository;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.model.StockItem;

public interface InventoryRepository {
    abstract void stockOut(StockItem stockItem);
    abstract void stockIn(StockItem stockItem);
    abstract StockItem[] getStockItems();
    abstract double getProductCost(ProductQuery productQuery);
    abstract float getStockLevelOf(ProductQuery productQuery);
}
