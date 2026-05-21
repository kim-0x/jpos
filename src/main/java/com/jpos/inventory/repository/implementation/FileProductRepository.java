package com.jpos.inventory.repository.implementation;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.repository.ProductRepository;
import utils.CsvRepositorySupport;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileProductRepository implements ProductRepository {
    private static final String DATA_LABEL = "Product data";
    private static final String[] HEADER = new String[] {"id", "barcode", "name", "category"};

    private final Path filePath;
    private final ArrayList<Product> products = new ArrayList<>();

    public FileProductRepository() {
        this(CsvRepositorySupport.getDefaultDataFilePath("product.csv"));
    }

    public FileProductRepository(Path filePath) {
        this.filePath = filePath;
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

        List<String[]> rows = CsvRepositorySupport.readRows(filePath, DATA_LABEL);
        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            String[] row = rows.get(rowIndex);
            if (row.length != 4) {
                throw new IllegalStateException(String.format("Invalid product row at line %d.", rowIndex + 1));
            }

            try {
                UUID productId = UUID.fromString(row[0].trim());
                Product product = new Product();
                product.setId(productId);
                product.setBarcode(row[1].trim());
                product.setName(row[2]);
                product.setCategory(row[3].trim());
                products.add(product);
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException(String.format("Invalid product id at line %d.", rowIndex + 1),
                        exception);
            }
        }
    }

    private void persistProducts() {
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(HEADER);

        for (Product product : products) {
            rows.add(new String[] {
                    product.getId().toString(),
                    product.getBarcode(),
                    product.getName(),
                    product.getCategory()
            });
        }

        CsvRepositorySupport.writeRows(filePath, DATA_LABEL, rows);
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
