package com.jpos.inventory.repository.implementation;

import com.jpos.inventory.model.Inventory;
import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.model.StockItem;
import com.jpos.inventory.repository.InventoryRepository;

public class MockInventoryRepository implements InventoryRepository {
    private static final Inventory inventory = new Inventory();

    @Override
    public void stockOut(StockItem stockItem) {
        validateStockItem(stockItem);

        if (inventory.isLowStockLevel(stockItem)) {
            throw new IllegalStateException(String.format("Product %s is low in stock.", stockItem.getProductId()));
        }

        stockItem.setNumberInStock(-Math.abs(stockItem.getNumberInStock()));
        inventory.addStockItem(stockItem);
    }

    @Override
    public void stockIn(StockItem stockItem) {
        validateStockItem(stockItem);
        stockItem.setNumberInStock(Math.abs(stockItem.getNumberInStock()));
        inventory.addStockItem(stockItem);
    }

    @Override
    public StockItem[] getStockItems() {
        return inventory.getStockItems();
    }

    @Override
    public double getProductCost(ProductQuery productQuery) {
        validateProductQuery(productQuery);
        return inventory.getLatestStockPrice(productQuery.getProductId());
    }

    @Override
    public float getStockLevelOf(ProductQuery productQuery) {
        validateProductQuery(productQuery);
        return inventory.getStockLevelForProduct(productQuery.getProductId());
    }

    private void validateProductQuery(ProductQuery productQuery) {
        if (productQuery == null) {
            throw new IllegalArgumentException("Product query is required.");
        }
        if (productQuery.getProductId() == null) {
            throw new IllegalArgumentException("Product id is required.");
        }
    }

    private void validateStockItem(StockItem stockItem) {
        if (stockItem == null) {
            throw new IllegalArgumentException("Stock item is required.");
        }
        if (stockItem.getProductId() == null) {
            throw new IllegalArgumentException("Product id is required.");
        }
    }
}
