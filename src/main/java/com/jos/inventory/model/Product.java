package com.jos.inventory.model;

import java.util.UUID;

public class Product implements Comparable<Product> {
    private UUID id;
    private String barcode;
    private String name;
    private String category;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public int compareTo(Product otherProduct) {
        if (otherProduct == null) {
            return 1;
        }

        boolean hasMatchingId = this.id != null
                && otherProduct.getId() != null
                && this.id.equals(otherProduct.getId());
        boolean hasMatchingBarcode = this.barcode != null
                && otherProduct.getBarcode() != null
                && this.barcode.equals(otherProduct.getBarcode());

        return hasMatchingId || hasMatchingBarcode ? 0 : 1;
    }
}
