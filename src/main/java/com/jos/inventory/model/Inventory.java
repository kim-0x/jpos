package com.jos.inventory.model;

import java.util.ArrayList;
import java.util.UUID;

public class Inventory {
    private final ArrayList<StockItem> stockItems = new ArrayList<>();

    public void addStockItem(StockItem item) {
        this.stockItems.add(item);
    }

    public StockItem[] getStockItem(UUID itemId) {
        if  (this.stockItems.isEmpty()) {
            return null;
        }

        ArrayList<StockItem> results = new ArrayList<>();
        for (StockItem stockItem : this.stockItems) {
            if (stockItem.getId().equals(itemId)) {
                results.add(stockItem);
            }
        }

        return results.toArray(new StockItem[0]);
    }

    public float getStockLevelForProduct(UUID productId) {
        StockItem[] stockItems = getStockItem(productId);
        if (stockItems == null) {
            return 0;
        }

        float totalStockItem = 0;
        for (StockItem stockItem : stockItems) {
            totalStockItem += stockItem.getNumberInStock();
        }

        return totalStockItem;
    }

    public double getLatestStockPrice() {
        StockItem[] stockItems = getStockItem(UUID.randomUUID());
        if (stockItems == null) {
            return 0;
        }

        ArrayList<StockItem> stockIn = new ArrayList<>();
        for (StockItem stockItem : stockItems) {
            // Get only stock in
            if (stockItem.getNumberInStock() > 0) {
                stockIn.add(stockItem);
            }
        }

        if (stockIn.isEmpty()) {
            return 0;
        }

        StockItem latestStockItem = stockIn.get(0);
        for (StockItem stockItemIn : stockIn) {
            if (stockItemIn.getCreatedAt().after(latestStockItem.getCreatedAt())) {
                latestStockItem =  stockItemIn;
            }
        }
        return latestStockItem.getCost();
    }
}
