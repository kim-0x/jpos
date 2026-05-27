package com.jpos.sale.service;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.sale.exception.ProductNotFoundException;

/**
 * Contract for retrieving product costs from external sources (e.g., inventory module).
 * Implementations are responsible for looking up the current cost of a product.
 */
public interface ProductCostProvider {
    /**
     * Get the current cost of a product.
     *
     * @param productQuery query containing product identifiers (id and/or barcode)
     * @return the cost of the product
     * @throws ProductNotFoundException if the product is not found
     */
    double getProductCost(ProductQuery productQuery);
}
