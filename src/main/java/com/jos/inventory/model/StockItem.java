package com.jos.inventory.model;

import java.util.Date;
import java.util.UUID;

public class StockItem {
    private UUID id;
    private float numberInStock;
    private double cost;
    private UUID productId;
    private Date createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public float getNumberInStock() {
        return numberInStock;
    }

    public void setNumberInStock(float numberInStock) {
        this.numberInStock = numberInStock;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
