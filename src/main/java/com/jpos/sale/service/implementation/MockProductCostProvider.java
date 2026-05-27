package com.jpos.sale.service.implementation;

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
    public double getProductCost(UUID productId) {
        if (!costMap.containsKey(productId)) {
            throw new ProductNotFoundException(productId);
        }
        return costMap.get(productId);
    }
}
