package com.jpos.inventory.repository.implementation;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.repository.ProductRepository;
import utils.AbstractCsvRepository;
import utils.CsvRepositorySupport;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

public class FileProductRepository extends AbstractCsvRepository<Product> implements ProductRepository {
    private static final String DATA_LABEL = "Product data";
    private static final String[] HEADER = new String[] {"id", "barcode", "name", "category"};

    private final ArrayList<Product> products = new ArrayList<>();

    public FileProductRepository() {
        this(CsvRepositorySupport.getDefaultDataFilePath("product.csv"));
    }

    public FileProductRepository(Path filePath) {
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

    private void loadProducts() {
        products.clear();
        products.addAll(loadFromCsv());
    }

    private void persistProducts() {
        persistToCsv(products);
    }

    @Override
    protected String getDataLabel() {
        return DATA_LABEL;
    }

    @Override
    protected String[] getHeaderRow() {
        return HEADER;
    }

    @Override
    protected Product toEntity(String[] row, int lineNumber) {
        if (row.length != 4) {
            throw new IllegalStateException(String.format("Invalid product row at line %d.", lineNumber));
        }

        try {
            Product product = new Product();
            product.setId(UUID.fromString(row[0].trim()));
            product.setBarcode(row[1].trim());
            product.setName(row[2]);
            product.setCategory(row[3].trim());
            return product;
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(String.format("Invalid product id at line %d.", lineNumber), exception);
        }
    }

    @Override
    protected String[] toRow(Product product) {
        return new String[] {
                product.getId().toString(),
                product.getBarcode(),
                product.getName(),
                product.getCategory()
        };
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
