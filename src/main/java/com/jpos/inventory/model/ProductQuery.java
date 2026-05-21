package com.jpos.inventory.model;

import java.util.UUID;

public class ProductQuery {
    private final UUID productId;
    private final String barcode;

    public UUID getProductId() {
        return productId;
    }

    public String getBarcode() {
        return barcode;
    }

    public ProductQuery(UUID productId, String barcode) {
        this.productId = productId;
        this.barcode = barcode;
    }
}
