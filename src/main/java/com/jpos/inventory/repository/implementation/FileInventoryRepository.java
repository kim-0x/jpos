package com.jpos.inventory.repository.implementation;

import com.jpos.inventory.model.Inventory;
import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.model.StockItem;
import com.jpos.inventory.repository.InventoryRepository;
import utils.CsvRepositorySupport;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class FileInventoryRepository implements InventoryRepository {
    private static final String DATA_LABEL = "Inventory data";
    private static final String[] HEADER = new String[] {"id", "numberInStock", "cost", "productId", "createdAt"};

    private final Path filePath;
    private final Inventory inventory = new Inventory();

    public FileInventoryRepository() {
        this(CsvRepositorySupport.getDefaultDataFilePath("inventory.csv"));
    }

    public FileInventoryRepository(Path filePath) {
        this.filePath = filePath;
        loadStockItems();
    }

    @Override
    public void stockOut(StockItem stockItem) {
        validateStockItem(stockItem);

        if (inventory.isLowStockLevel(stockItem)) {
            throw new IllegalStateException(String.format("Product %s is low in stock.", stockItem.getProductId()));
        }

        if (stockItem.getId() == null) {
            stockItem.setId(UUID.randomUUID());
        }
        if (stockItem.getCreatedAt() == null) {
            stockItem.setCreatedAt(new Date());
        }

        stockItem.setNumberInStock(-Math.abs(stockItem.getNumberInStock()));
        inventory.addStockItem(stockItem);
        persistStockItems();
    }

    @Override
    public void stockIn(StockItem stockItem) {
        validateStockItem(stockItem);

        if (stockItem.getId() == null) {
            stockItem.setId(UUID.randomUUID());
        }
        if (stockItem.getCreatedAt() == null) {
            stockItem.setCreatedAt(new Date());
        }

        stockItem.setNumberInStock(Math.abs(stockItem.getNumberInStock()));
        inventory.addStockItem(stockItem);
        persistStockItems();
    }

    @Override
    public StockItem[] getStockItems() {
        return inventory.getStockItems();
    }

    @Override
    public double getProductCost(ProductQuery productQuery) {
        validateProductQuery(productQuery);
        return inventory.getLatestStockPrice(productQuery.getProductId());
    }

    @Override
    public float getStockLevelOf(ProductQuery productQuery) {
        validateProductQuery(productQuery);
        return inventory.getStockLevelForProduct(productQuery.getProductId());
    }

    private void loadStockItems() {
        List<String[]> rows = CsvRepositorySupport.readRows(filePath, DATA_LABEL);
        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            String[] row = rows.get(rowIndex);
            if (row.length != 5) {
                throw new IllegalStateException(String.format("Invalid inventory row at line %d.", rowIndex + 1));
            }

            try {
                StockItem stockItem = new StockItem();
                stockItem.setId(UUID.fromString(row[0].trim()));
                stockItem.setNumberInStock(Float.parseFloat(row[1]));
                stockItem.setCost(Double.parseDouble(row[2]));
                stockItem.setProductId(UUID.fromString(row[3].trim()));
                stockItem.setCreatedAt(CsvRepositorySupport.parseTimestamp(row[4]));
                inventory.addStockItem(stockItem);
            } catch (RuntimeException exception) {
                throw new IllegalStateException(String.format("Invalid inventory row at line %d.", rowIndex + 1),
                        exception);
            }
        }
    }

    private void persistStockItems() {
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(HEADER);

        for (StockItem stockItem : inventory.getStockItems()) {
            rows.add(new String[] {
                    stockItem.getId().toString(),
                    String.valueOf(stockItem.getNumberInStock()),
                    String.valueOf(stockItem.getCost()),
                    stockItem.getProductId().toString(),
                    CsvRepositorySupport.formatTimestamp(stockItem.getCreatedAt())
            });
        }

        CsvRepositorySupport.writeRows(filePath, DATA_LABEL, rows);
    }

    private void validateProductQuery(ProductQuery productQuery) {
        if (productQuery == null) {
            throw new IllegalArgumentException("Product query is required.");
        }
        if (productQuery.getProductId() == null) {
            throw new IllegalArgumentException("Product id is required.");
        }
    }

    private void validateStockItem(StockItem stockItem) {
        if (stockItem == null) {
            throw new IllegalArgumentException("Stock item is required.");
        }
        if (stockItem.getProductId() == null) {
            throw new IllegalArgumentException("Product id is required.");
        }
    }
}
