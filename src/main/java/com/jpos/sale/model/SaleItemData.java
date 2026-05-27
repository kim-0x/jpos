package com.jpos.sale.model;

import java.util.UUID;

public class SaleItemData {
    private final UUID productId;
    private final float quantity;
    private final double price;

    public SaleItemData(UUID productId, float quantity, double price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public UUID getProductId() {
        return productId;
    }

    public float getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
}
