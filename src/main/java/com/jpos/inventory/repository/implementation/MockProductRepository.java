package com.jpos.inventory.repository.implementation;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.repository.ProductRepository;

import java.util.ArrayList;
import java.util.UUID;

public class MockProductRepository implements ProductRepository {
    private static final ArrayList<Product> products = new ArrayList<>();

    @Override
    public Product getProductBy(ProductQuery productQuery) {
        int existingProductIndex = findExistingProductIndex(toProduct(productQuery));
        return existingProductIndex >= 0 ? products.get(existingProductIndex) : null;
    }

    @Override
    public Product[] getProducts() {
        return products.toArray(new Product[0]);
    }

    @Override
    public void saveProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product is required.");
        }

        int existingProductIndex = findExistingProductIndex(product);
        if (existingProductIndex >= 0) {
            Product existingProduct = products.get(existingProductIndex);
            if (product.getId() == null) {
                product.setId(existingProduct.getId());
            }
            products.set(existingProductIndex, product);
            return;
        }

        if (product.getId() == null) {
            product.setId(UUID.randomUUID());
        }

        products.add(product);
    }

    private int findExistingProductIndex(Product product) {
        if (product == null) {
            return -1;
        }

        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).compareTo(product) == 0) {
                return i;
            }
        }

        return -1;
    }

    private Product toProduct(ProductQuery productQuery) {
        if (productQuery == null) {
            return null;
        }

        Product product = new Product();
        product.setId(productQuery.getProductId());
        product.setBarcode(productQuery.getBarcode());
        return product;
    }
}
