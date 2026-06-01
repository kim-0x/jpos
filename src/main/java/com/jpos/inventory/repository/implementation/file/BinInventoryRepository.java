package com.jpos.inventory.repository.implementation.file;

import com.jpos.inventory.model.Inventory;
import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.model.StockItem;
import com.jpos.inventory.repository.InventoryRepository;
import utils.AbstractBinRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class BinInventoryRepository extends AbstractBinRepository<StockItem> implements InventoryRepository {
    private final Inventory inventory = new Inventory();

    public BinInventoryRepository() {
        this(getDefaultDatFilePath("inventory.dat"));
    }

    public BinInventoryRepository(Path filePath) {
        super(filePath);
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

    @Override
    protected Class<StockItem> getEntityType() {
        return StockItem.class;
    }

    private void loadStockItems() {
        for (StockItem stockItem : loadFromDat()) {
            inventory.addStockItem(stockItem);
        }
    }

    private void persistStockItems() {
        ArrayList<StockItem> stockItems = new ArrayList<>();
        for (StockItem stockItem : inventory.getStockItems()) {
            stockItems.add(stockItem);
        }
        persistToDat(stockItems);
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
