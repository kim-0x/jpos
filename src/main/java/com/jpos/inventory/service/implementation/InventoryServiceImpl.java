package com.jpos.inventory.service.implementation;

import com.jpos.inventory.exception.ProductNotFoundException;
import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.StockRecord;
import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.model.StockItem;
import com.jpos.inventory.repository.InventoryRepository;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.inventory.service.InventoryService;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void entryStock(String barcode, double cost, float numberInStock) {
        Product product = productRepository.getProductBy(new ProductQuery(null, barcode));
        if (product == null) {
            throw new ProductNotFoundException(barcode);
        }

        StockItem stockItem = new StockItem();
        stockItem.setId(UUID.randomUUID());
        stockItem.setProductId(product.getId());
        stockItem.setNumberInStock(numberInStock);
        stockItem.setCost(cost);
        stockItem.setCreatedAt(new Date());

        inventoryRepository.stockIn(stockItem);
    }

    @Override
    public void reduceStock(ProductQuery productQuery, float numberOfStock) {
        if (numberOfStock <= 0) {
            throw new IllegalArgumentException("Number of stock must be greater than zero.");
        }

        Product product = productRepository.getProductBy(productQuery);
        if (product == null) {
            throw new ProductNotFoundException(productQuery == null
                    ? "null"
                    : String.valueOf(productQuery.getProductId() != null
                    ? productQuery.getProductId()
                    : productQuery.getBarcode()));
        }

        ProductQuery normalizedQuery = new ProductQuery(product.getId(), product.getBarcode());
        StockItem stockItem = new StockItem();
        stockItem.setId(UUID.randomUUID());
        stockItem.setProductId(product.getId());
        stockItem.setNumberInStock(numberOfStock);
        stockItem.setCost(inventoryRepository.getProductCost(normalizedQuery));
        stockItem.setCreatedAt(new Date());

        inventoryRepository.stockOut(stockItem);
    }

    @Override
    public StockRecord[] getStockReport() {
        ArrayList<StockRecord> stockRecords = new ArrayList<>();
        for (Product product : productRepository.getProducts()) {
            ProductQuery productQuery = new ProductQuery(product.getId(), product.getBarcode());
            double cost = inventoryRepository.getProductCost(productQuery);
            float numberInStock = inventoryRepository.getStockLevelOf(productQuery);
            stockRecords.add(new StockRecord(product, cost, numberInStock));
        }
        return stockRecords.toArray(new StockRecord[0]);
    }

    @Override
    public double getProductCostBy(ProductQuery productQuery) {
        Product product = productRepository.getProductBy(productQuery);

        if (product == null) {
            throw new ProductNotFoundException(productQuery == null
                    ? "null"
                    : String.valueOf(productQuery.getProductId() != null
                    ? productQuery.getProductId()
                    : productQuery.getBarcode()));
        }

        ProductQuery normalizedQuery = new ProductQuery(product.getId(), product.getBarcode());
        return inventoryRepository.getProductCost(normalizedQuery);
    }
}
