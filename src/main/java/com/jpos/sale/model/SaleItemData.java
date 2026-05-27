package com.jpos.sale.model;

public class SaleItemData {
    private final String barcode;
    private final float quantity;

    public SaleItemData(String barcode, float quantity) {
        this.barcode = barcode;
        this.quantity = quantity;
    }

    public String getBarcode() {
        return barcode;
    }

    public float getQuantity() {
        return quantity;
    }
}
