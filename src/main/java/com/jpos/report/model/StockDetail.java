package com.jpos.report.model;

import java.util.Date;
import java.util.UUID;

public class StockDetail {
    private final UUID productId;
    private final String productName;
    private final double latestCost;
    private final float totalNumberInStock;
    private final Date lastModifiedAt;
    private final float lowStockLevel = 3.0f;

    public StockDetail(UUID productId, String productName, double latestCost, float totalNumberInStock, Date lastModifiedAt) {
        this.productId = productId;
        this.productName = productName;
        this.latestCost = latestCost;
        this.totalNumberInStock = totalNumberInStock;
        this.lastModifiedAt = lastModifiedAt;
    }

    public UUID getProductId() { return productId; }

    public String getProductName() {
        return productName;
    }

    public double getLatestCost() {
        return latestCost;
    }

    public float getTotalNumberInStock() {
        return totalNumberInStock;
    }

    public double getTotalStockValue() {
        return totalNumberInStock * latestCost;
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
                (this.lastModifiedAt.before(other.lastModifiedAt)) ? other.latestCost : this.latestCost,
                this.totalNumberInStock + other.totalNumberInStock,
                (this.lastModifiedAt.before(other.lastModifiedAt)) ? other.lastModifiedAt : this.lastModifiedAt
        );
    }
}
