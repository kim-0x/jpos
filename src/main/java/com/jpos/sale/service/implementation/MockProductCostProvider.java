package com.jpos.sale.service.implementation;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.sale.exception.ProductNotFoundException;
import com.jpos.sale.service.ProductCostProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MockProductCostProvider implements ProductCostProvider {
    private final Map<UUID, Double> costMap = new HashMap<>();

    public void registerProductCost(UUID productId, double cost) {
        costMap.put(productId, cost);
    }

    @Override
    public double getProductCost(ProductQuery productQuery) {
        UUID productId = productQuery == null ? null : productQuery.getProductId();
        if (productId == null || !costMap.containsKey(productId)) {
            throw new ProductNotFoundException(String.valueOf(productId));
        }
        return costMap.get(productId);
    }
}
