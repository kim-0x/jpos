package com.jpos.inventory.service;

import com.jpos.inventory.model.Product;

public interface ProductService {
    abstract void saveProduct(String barcode, String name, String category);
    abstract Product[] getProducts();
    abstract String[] getCategories();
}
