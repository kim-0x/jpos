package com.jpos.sale.service;

import com.jpos.inventory.model.ProductQuery;

import java.util.UUID;

/**
 * Resolves a product identifier from flexible product query input.
 */
public interface ProductIdentifierProvider {
    UUID getProductId(ProductQuery productQuery);
}
