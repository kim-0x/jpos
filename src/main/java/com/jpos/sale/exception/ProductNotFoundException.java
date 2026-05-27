package com.jpos.sale.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(UUID productId) {
        super(String.format("Product %s not found.", productId));
    }

    public ProductNotFoundException(String productIdentifier) {
        super(String.format("Product %s not found.", productIdentifier));
    }
}
