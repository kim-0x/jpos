package com.jpos.inventory.service.implementation;

import com.jpos.inventory.exception.InvalidCategoryException;
import com.jpos.inventory.model.Product;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.inventory.service.ProductService;

import java.util.Arrays;

public class ProductServiceImpl implements ProductService {
    private final String[] categories = new String[] {"food", "beverage", "household", "fruit", "dairy"};
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void saveProduct(String barcode, String name, String category) {
        String normalizedCategory = category == null ? null : category.trim().toLowerCase();
        if (normalizedCategory == null || !Arrays.asList(categories).contains(normalizedCategory)) {
            throw new InvalidCategoryException(category);
        }

        Product product = new Product();
        product.setBarcode(barcode);
        product.setName(name);
        product.setCategory(normalizedCategory);

        productRepository.saveProduct(product);
    }

    @Override
    public Product[] getProducts() {
        return this.productRepository.getProducts();
    }

    @Override
    public String[] getCategories() {
        return categories;
    }
}
