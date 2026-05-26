package com.jpos.sale.service;

import java.util.UUID;
import com.jpos.sale.exception.ProductNotFoundException;

/**
 * Contract for retrieving product costs from external sources (e.g., inventory module).
 * Implementations are responsible for looking up the current cost of a product by its ID.
 */
public interface ProductCostProvider {
    /**
     * Get the current cost of a product.
     *
     * @param productId the unique identifier of the product
     * @return the cost of the product
     * @throws ProductNotFoundException if the product is not found
     */
    double getProductCost(UUID productId);
}
