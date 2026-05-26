package com.jpos.sale.service;

import com.jpos.sale.model.PriceBook;

import java.util.UUID;

public interface ProductPriceService {
    abstract void setProductPrice(UUID productId, float margin);
    abstract PriceBook getCurrentProductPrice(UUID productId);
}
