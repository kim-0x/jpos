package com.jos.inventory.service.implementation;

import com.jos.inventory.model.Product;
import com.jos.inventory.model.ProductInventoryRecord;
import com.jos.inventory.model.ProductQuery;
import com.jos.inventory.model.StockItem;
import com.jos.inventory.repository.InventoryRepository;
import com.jos.inventory.repository.ProductRepository;
import com.jos.inventory.service.InventoryService;

import java.util.ArrayList;
import java.util.Date;

public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void entryStock(String barcode, String name, String category, double cost, float numberInStock) {
        Product product = productRepository.getProductBy(new ProductQuery(null, barcode));
        if (product == null) {
            throw new IllegalArgumentException(String.format("Product %s not found.", barcode));
        }

        StockItem stockItem = new StockItem();
        stockItem.setProductId(product.getId());
        stockItem.setNumberInStock(numberInStock);
        stockItem.setCost(cost);
        stockItem.setCreatedAt(new Date());

        inventoryRepository.stockIn(stockItem);
    }

    @Override
    public ProductInventoryRecord[] getProductsWithLatestCost() {
        ArrayList<ProductInventoryRecord> products = new ArrayList<>();
        for (Product product : productRepository.getProducts()) {
            ProductQuery productQuery = new ProductQuery(product.getId(), product.getBarcode());
            double cost = inventoryRepository.getProductCost(productQuery);
            float numberInStock = inventoryRepository.getStockLevelOf(productQuery);
            products.add(new ProductInventoryRecord(product, cost, numberInStock));
        }
        return products.toArray(new ProductInventoryRecord[0]);
    }
}
