package com.jos.inventory.repository.implementation;

import com.jos.inventory.model.Product;
import com.jos.inventory.model.ProductQuery;
import com.jos.inventory.repository.ProductRepository;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class MockProductRepository implements ProductRepository {
    private static final ArrayList<Product> products = new ArrayList<>();

    @Override
    public Product getProductBy(ProductQuery productQuery) {
        if (productQuery == null) {
            return null;
        }

        for (Product product : products) {
            if ((productQuery.getProductId() != null && Objects.equals(product.getId(), productQuery.getProductId()))
                    || (productQuery.getBarcode() != null && Objects.equals(product.getBarcode(), productQuery.getBarcode()))) {
                return product;
            }
        }

        return null;
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
        for (int i = 0; i < products.size(); i++) {
            Product existingProduct = products.get(i);
            if ((product.getId() != null && Objects.equals(existingProduct.getId(), product.getId()))
                    || (product.getBarcode() != null && Objects.equals(existingProduct.getBarcode(), product.getBarcode()))) {
                return i;
            }
        }

        return -1;
    }
}
