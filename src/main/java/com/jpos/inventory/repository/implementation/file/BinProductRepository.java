package com.jpos.inventory.repository.implementation.file;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.repository.ProductRepository;
import utils.AbstractBinRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

public class BinProductRepository extends AbstractBinRepository<Product> implements ProductRepository {
    private final ArrayList<Product> products = new ArrayList<>();

    public BinProductRepository() {
        this(getDefaultDatFilePath("product.dat"));
    }

    public BinProductRepository(Path filePath) {
        super(filePath);
        loadProducts();
    }

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
            product.setId(existingProduct.getId());
            validateUniqueProduct(product, existingProductIndex);
            products.set(existingProductIndex, product);
            persistProducts();
            return;
        }

        if (product.getId() == null) {
            product.setId(UUID.randomUUID());
        }

        validateUniqueProduct(product, -1);
        products.add(product);
        persistProducts();
    }

    @Override
    protected Class<Product> getEntityType() {
        return Product.class;
    }

    private void loadProducts() {
        products.clear();
        products.addAll(loadFromDat());
    }

    private void persistProducts() {
        persistToDat(products);
    }

    private void validateUniqueProduct(Product product, int excludedIndex) {
        for (int index = 0; index < products.size(); index++) {
            if (index == excludedIndex) {
                continue;
            }

            if (product.compareTo(products.get(index)) == 0) {
                throw new IllegalStateException("Product id or barcode already exists.");
            }
        }
    }

    private int findExistingProductIndex(Product product) {
        if (product == null) {
            return -1;
        }

        for (int index = 0; index < products.size(); index++) {
            if (products.get(index).compareTo(product) == 0) {
                return index;
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
