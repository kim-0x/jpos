package com.jpos.sale.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class PriceBook implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final UUID productId;
    private final double cost;
    private final float margin;
    private final double salePrice;
    private final Date effectiveAt;

    public PriceBook(UUID productId, double cost, float margin, double salePrice) {
        this(productId, cost, margin, salePrice, null);
    }

    public PriceBook(UUID productId, double cost, float margin, double salePrice, Date effectiveAt) {
        if (productId == null) {
            throw new IllegalArgumentException("Product id is required.");
        }
        if (cost <= 0) {
            throw new IllegalArgumentException("Cost must be a positive number.");
        }
        if (salePrice <= 0) {
            throw new IllegalArgumentException("Sale price must be a positive number.");
        }

        this.productId = productId;
        this.cost = cost;
        this.margin = margin;
        this.salePrice = salePrice;
        Date timestamp = (effectiveAt == null) ? new Date() : effectiveAt;
        this.effectiveAt = new Date(timestamp.getTime());
    }

    public UUID getProductId() {
        return productId;
    }

    public double getCost() {
        return cost;
    }

    public float getMargin() {
        return margin;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public Date getEffectiveAt() {
        return new Date(effectiveAt.getTime());
    }
}
