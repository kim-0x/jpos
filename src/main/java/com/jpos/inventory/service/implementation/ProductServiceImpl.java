package com.jpos.inventory.service.implementation;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductCategory;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.inventory.service.ProductService;

public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void saveProduct(String barcode, String name, ProductCategory category) {
        Product product = new Product();
        product.setBarcode(barcode);
        product.setName(name);
        product.setCategory(category);

        productRepository.saveProduct(product);
    }

    @Override
    public Product[] getProducts() {
        return this.productRepository.getProducts();
    }

    @Override
    public ProductCategory[] getCategories() {
        return ProductCategory.values();
    }
}