package com.jpos.sale.service;

import com.jpos.sale.model.ProductInfo;
import com.jpos.sale.model.ProductRef;

public interface ProductCatalogGateway {
    ProductInfo findBy(ProductRef ref);
}