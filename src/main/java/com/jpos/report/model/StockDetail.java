package com.jpos.report.model;

import java.util.UUID;

public class StockDetail {
    private final UUID productId;
    private final String productName;
    private final double totalCost;
    private final float totalNumberInStock;
    private final double totalStockValue;
    private final float lowStockLevel = 3.0f;

    public StockDetail(UUID productId, String productName, double totalCost, float totalNumberInStock, double totalStockValue) {
        this.productId = productId;
        this.productName = productName;
        this.totalCost = totalCost;
        this.totalNumberInStock = totalNumberInStock;
        this.totalStockValue = totalStockValue;
    }

    public UUID getProductId() { return productId; }

    public String getProductName() {
        return productName;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public float getTotalNumberInStock() {
        return totalNumberInStock;
    }

    public double getTotalStockValue() {
        return totalStockValue;
    }

    public ReorderStatus getReorderStatus() {
        return  (this.getTotalNumberInStock() == 0) ?
                ReorderStatus.OUT_OF_STOCK :
                (this.getTotalNumberInStock() < lowStockLevel) ?
                ReorderStatus.LOW_STOCK : ReorderStatus.IN_STOCK;
    }

    public StockDetail accumulate(StockDetail other) {
        return new StockDetail(
                this.productId,
                this.productName,
                this.totalCost + other.totalCost,
                this.totalNumberInStock + other.totalNumberInStock,
                this.totalStockValue + other.totalStockValue
        );
    }
}
