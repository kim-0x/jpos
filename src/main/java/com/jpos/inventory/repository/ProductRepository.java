package com.jpos.inventory.repository;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductQuery;

public interface ProductRepository {
    abstract Product getProductBy(ProductQuery productQuery);
    abstract Product[] getProducts();
    abstract void saveProduct(Product product);
}
