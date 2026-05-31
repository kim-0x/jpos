package com.jpos.inventory.model;

import com.jpos.inventory.exception.InvalidCategoryException;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public class Product implements Comparable<Product>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private UUID id;
    private String barcode;
    private String name;
    private ProductCategory category;

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

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        if (category == null) {
            throw new InvalidCategoryException("Invalid category");
        }

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
