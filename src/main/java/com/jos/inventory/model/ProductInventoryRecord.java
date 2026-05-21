package com.jos.inventory.model;

public class ProductInventoryRecord {
    private final Product product;
    private final double cost;
    private final double numberInStock;

    public ProductInventoryRecord(Product product, double cost, double numberInStock) {
        this.product = product;
        this.cost = cost;
        this.numberInStock = numberInStock;
    }

    public Product getProduct() {
        return product;
    }

    public double getCost() {
        return cost;
    }

    public double getNumberInStock() {
        return numberInStock;
    }
}
