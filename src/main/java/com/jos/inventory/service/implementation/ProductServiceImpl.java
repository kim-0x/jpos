package com.jos.inventory.service.implementation;

import com.jos.inventory.model.Product;
import com.jos.inventory.repository.ProductRepository;
import com.jos.inventory.service.ProductService;

import java.security.InvalidParameterException;
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
            throw new InvalidParameterException(String.format("Invalid category %s", category));
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
