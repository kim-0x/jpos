package com.jpos.sale.service.implementation;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.sale.exception.ProductNotFoundException;
import com.jpos.sale.service.ProductIdentifierProvider;

import java.util.UUID;

public class InventoryProductIdentifierProvider implements ProductIdentifierProvider {
    private final ProductRepository productRepository;

    public InventoryProductIdentifierProvider(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public UUID getProductId(ProductQuery productQuery) {
        Product product = productRepository.getProductBy(productQuery);
        if (product == null) {
            String identifier = productQuery == null
                    ? "null"
                    : String.valueOf(productQuery.getProductId() != null
                    ? productQuery.getProductId()
                    : productQuery.getBarcode());
            throw new ProductNotFoundException(identifier);
        }

        return product.getId();
    }
}
