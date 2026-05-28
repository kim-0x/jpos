package com.jpos.inventory;

import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductCategory;
import com.jpos.inventory.model.StockRecord;
import com.jpos.inventory.repository.InventoryRepository;
import com.jpos.inventory.repository.ProductRepository;
import com.jpos.inventory.service.InventoryService;
import com.jpos.inventory.service.ProductService;
import com.jpos.inventory.service.implementation.InventoryServiceImpl;
import com.jpos.inventory.service.implementation.ProductServiceImpl;

public class InventoryFacade {
    private final InventoryService inventoryService;
    private final ProductService productService;

    public InventoryFacade(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryService = new InventoryServiceImpl(inventoryRepository,  productRepository);
        this.productService = new ProductServiceImpl(productRepository);
    }

    public void createNewProduct(String barcode, String name, ProductCategory category) {
        productService.saveProduct(barcode, name, category);
    }

    public Product[] getProducts() {
        return productService.getProducts();
    }

    public StockRecord[] getStockReport() {
        return inventoryService.getStockReport();
    }

    public void stockEntry(String barcode, double cost, float numberInStock) {
        inventoryService.entryStock(barcode, cost, numberInStock);
    }
}
