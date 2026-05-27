package com.jpos.sale.service.implementation;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.sale.exception.ProductNotFoundException;
import com.jpos.sale.service.ProductIdentifierProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MockProductIdentifierProvider implements ProductIdentifierProvider {
    private final Map<String, UUID> barcodeToProductId = new HashMap<>();

    public void registerProduct(String barcode, UUID productId) {
        if (barcode != null) {
            barcodeToProductId.put(barcode, productId);
        }
    }

    @Override
    public UUID getProductId(ProductQuery productQuery) {
        if (productQuery == null) {
            throw new ProductNotFoundException("null");
        }

        String barcode = productQuery.getBarcode();
        if (barcode != null && barcodeToProductId.containsKey(barcode)) {
            return barcodeToProductId.get(barcode);
        }

        if (productQuery.getProductId() != null) {
            return productQuery.getProductId();
        }

        throw new ProductNotFoundException(String.valueOf(barcode));
    }
}
