package com.jpos.report.model;

public class StockDetail {
    private final String productName;
    private final double totalCost;
    private final float totalNumberInStock;
    private final double totalStockValue;
    private final ReorderStatus reorderStatus;

    public StockDetail(String productName, double totalCost, float totalNumberInStock, double totalStockValue, ReorderStatus reorderStatus) {
        this.productName = productName;
        this.totalCost = totalCost;
        this.totalNumberInStock = totalNumberInStock;
        this.totalStockValue = totalStockValue;
        this.reorderStatus = reorderStatus;
    }

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
        return reorderStatus;
    }
}
