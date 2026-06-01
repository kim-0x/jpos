package com.jpos.sale.service;

import com.jpos.sale.model.ProductInfo;
import com.jpos.sale.model.ProductRef;

public interface InventoryGateway {
    ProductInfo findBy(ProductRef ref);
    void reduceStock(ProductRef ref, float numberOfStock);
}