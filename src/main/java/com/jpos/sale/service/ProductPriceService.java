package com.jpos.sale.service;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.sale.model.PriceBook;

public interface ProductPriceService {
    abstract void setProductPrice(ProductQuery productQuery, float margin);
    abstract PriceBook getCurrentProductPrice(ProductQuery productQuery);
}
