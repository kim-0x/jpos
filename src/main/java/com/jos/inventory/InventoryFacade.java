package com.jos.inventory;

import com.jos.inventory.model.Product;
import com.jos.inventory.model.ProductInventoryRecord;
import com.jos.inventory.repository.InventoryRepository;
import com.jos.inventory.repository.ProductRepository;
import com.jos.inventory.service.InventoryService;
import com.jos.inventory.service.ProductService;
import com.jos.inventory.service.implementation.InventoryServiceImpl;
import com.jos.inventory.service.implementation.ProductServiceImpl;

public class InventoryFacade {
    private final InventoryService inventoryService;
    private final ProductService productService;

    public InventoryFacade(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryService = new InventoryServiceImpl(inventoryRepository,  productRepository);
        this.productService = new ProductServiceImpl(productRepository);
    }

    public void createNewProduct(String barcode, String name, String category) {
        productService.saveProduct(barcode, name, category);
    }

    public Product[] getProducts() {
        return productService.getProducts();
    }

    public String[] getCategories() {
        return productService.getCategories();
    }

    public ProductInventoryRecord[] getProductsWithLatestCost() {
        return inventoryService.getProductsWithLatestCost();
    }

    public void stockEntry(String barcode, String name, String category, double cost, float numberInStock) {
        inventoryService.entryStock(barcode, name, category, cost, numberInStock);
    }
}
