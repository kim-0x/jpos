package com.jos.inventory.repository;

import com.jos.inventory.model.Product;
import com.jos.inventory.model.ProductQuery;

public interface ProductRepository {
    abstract Product getProductBy(ProductQuery productQuery);
    abstract Product[] getProducts();
    abstract void saveProduct(Product product);
}
