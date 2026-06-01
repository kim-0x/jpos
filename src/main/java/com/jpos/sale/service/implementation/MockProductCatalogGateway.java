package com.jpos.sale.service.implementation;

import com.jpos.sale.exception.ProductNotFoundException;
import com.jpos.sale.model.ProductInfo;
import com.jpos.sale.model.ProductRef;
import com.jpos.sale.service.ProductCatalogGateway;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MockProductCatalogGateway implements ProductCatalogGateway {
    private final Map<UUID, ProductInfo> byProductId = new HashMap<>();
    private final Map<String, UUID> barcodeToProductId = new HashMap<>();

    public void registerProduct(String barcode, UUID productId, String name, double cost) {
        ProductInfo productInfo = new ProductInfo(productId, barcode, name, cost);
        byProductId.put(productId, productInfo);
        if (barcode != null) {
            barcodeToProductId.put(barcode, productId);
        }
    }

    public void registerProductCost(UUID productId, double cost) {
        ProductInfo existing = byProductId.get(productId);
        if (existing == null) {
            byProductId.put(productId, new ProductInfo(productId, null, "Unknown", cost));
            return;
        }

        byProductId.put(productId, new ProductInfo(existing.productId(), existing.barcode(), existing.name(), cost));
    }

    @Override
    public ProductInfo findBy(ProductRef ref) {
        if (ref == null) {
            throw new ProductNotFoundException("null");
        }

        UUID productId = ref.productId();
        if (productId == null && ref.barcode() != null) {
            productId = barcodeToProductId.get(ref.barcode());
        }

        ProductInfo productInfo = productId == null ? null : byProductId.get(productId);
        if (productInfo == null) {
            throw new ProductNotFoundException(String.valueOf(productId != null ? productId : ref.barcode()));
        }

        return productInfo;
    }

    @Override
    public void reduceStock(ProductRef ref, float numberOfStock) {
        if (numberOfStock <= 0) {
            throw new IllegalArgumentException("Number of stock must be greater than zero.");
        }

        // For mock behavior, validating that the referenced product exists is sufficient.
        findBy(ref);
    }
}