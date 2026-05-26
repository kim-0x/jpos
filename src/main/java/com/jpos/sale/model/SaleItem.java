package com.jpos.sale.model;

import java.util.UUID;

public class SaleItem {
    private final UUID productId;
    private final float quantity;
    private final double price;

    public double getTotalPrice() {
        return quantity * price;
    }

    private final UUID transactionId;

    public UUID getProductId() {
        return productId;
    }

    public float getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public SaleItem(UUID productId, float quantity, double price, UUID transactionId) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.transactionId = transactionId;
    }
}
