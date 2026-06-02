package com.jpos.sale.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public class SaleItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final UUID productId;
    private final float quantity;
    private final double cost;
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

    public double getCost() {
        return cost;
    }

    public double getPrice() {
        return price;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public SaleItem(UUID productId, float quantity, double cost, double price, UUID transactionId) {
        this.productId = productId;
        this.quantity = quantity;
        this.cost = cost;
        this.price = price;
        this.transactionId = transactionId;
    }
}
