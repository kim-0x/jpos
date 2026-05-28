package com.jpos.inventory.service;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductCategory;

public interface ProductService {
    void saveProduct(String barcode, String name, ProductCategory category);
    Product[] getProducts();
    ProductCategory[] getCategories();
}
