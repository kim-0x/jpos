package com.jpos.inventory.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String barcode) {
        super(String.format("Product %s not found.", barcode));
    }
}
