package com.jpos.inventory.model;

import java.util.Date;

public class StockRecord {
    private final Product product;
    private final double cost;
    private final double numberInStock;
    private final Date createdAt;

    public StockRecord(Product product, double cost, double numberInStock, Date createdAt) {
        this.product = product;
        this.cost = cost;
        this.numberInStock = numberInStock;
        this.createdAt = createdAt;
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

    public Date getCreatedAt() {
        return createdAt;
    }
}
