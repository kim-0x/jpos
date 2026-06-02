package com.jpos.report.model;

public class SaleDetail {
    private final String productName;
    private final float totalQuantity;
    private final double totalCost;
    private final double totalPrice;

    public SaleDetail(String productName, float totalQuantity, double totalCost, double totalPrice) {
        this.productName = productName;
        this.totalQuantity = totalQuantity;
        this.totalCost = totalCost;
        this.totalPrice = totalPrice;
    }

    public String getProductName() {
        return productName;
    }

    public float getTotalQuantity() {
        return totalQuantity;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public SaleDetail accumulate(SaleDetail other) {
        return new SaleDetail(
                this.productName,
                this.totalQuantity + other.totalQuantity,
                this.totalCost + other.totalCost,
                this.totalPrice + other.totalPrice
        );
    }
}
