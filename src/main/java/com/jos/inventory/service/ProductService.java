package com.jos.inventory.service;

import com.jos.inventory.model.Product;

import java.util.UUID;

public interface ProductService {
    abstract void saveProduct(String barcode, String name, String category);
    abstract Product[] getProducts();
}
