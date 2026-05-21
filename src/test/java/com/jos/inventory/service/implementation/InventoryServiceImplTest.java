package com.jos.inventory.service.implementation;

import com.jos.inventory.model.Inventory;
import com.jos.inventory.model.Product;
import com.jos.inventory.model.StockItem;
import com.jos.inventory.repository.implementation.MockInventoryRepository;
import com.jos.inventory.repository.implementation.MockProductRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.*;

public class InventoryServiceImplTest {
    private InventoryServiceImpl inventoryService;
    private MockInventoryRepository inventoryRepository;
    private MockProductRepository productRepository;

    @Before
    public void setUp() throws Exception {
        resetProducts();
        resetInventory();
        inventoryRepository = new MockInventoryRepository();
        productRepository = new MockProductRepository();
        inventoryService = new InventoryServiceImpl(inventoryRepository, productRepository);
    }

    @After
    public void tearDown() throws Exception {
        resetProducts();
        resetInventory();
    }

    @Test
    public void shouldEntryStockWhenProductExists() {
        productRepository.saveProduct(createProduct("barcode-1", "Milk", "dairy"));
        Date beforeEntry = new Date();

        inventoryService.entryStock("barcode-1", "Milk", "dairy", 4.5, 5);

        Date afterEntry = new Date();
        StockItem[] stockItems = inventoryRepository.getStockItems();

        assertEquals(1, stockItems.length);
        assertNotNull(stockItems[0].getCreatedAt());
        assertEquals(5.0f, stockItems[0].getNumberInStock(), 0.0f);
        assertEquals(4.5, stockItems[0].getCost(), 0.0);
        assertEquals(productRepository.getProducts()[0].getId(), stockItems[0].getProductId());
        assertFalse(stockItems[0].getCreatedAt().before(beforeEntry));
        assertFalse(stockItems[0].getCreatedAt().after(afterEntry));
    }

    @Test
    public void shouldThrowWhenProductDoesNotExist() {
        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.entryStock("missing-barcode", "Milk", "dairy", 4.5, 5));
    }

    private Product createProduct(String barcode, String name, String category) {
        Product product = new Product();
        product.setBarcode(barcode);
        product.setName(name);
        product.setCategory(category);
        return product;
    }

    @SuppressWarnings("unchecked")
    private void resetProducts() throws Exception {
        Field productsField = MockProductRepository.class.getDeclaredField("products");
        productsField.setAccessible(true);
        ArrayList<Product> products = (ArrayList<Product>) productsField.get(null);
        products.clear();
    }

    @SuppressWarnings("unchecked")
    private void resetInventory() throws Exception {
        Field inventoryField = MockInventoryRepository.class.getDeclaredField("inventory");
        inventoryField.setAccessible(true);
        Inventory inventory = (Inventory) inventoryField.get(null);

        Field inventoryStockItemsField = Inventory.class.getDeclaredField("stockItems");
        inventoryStockItemsField.setAccessible(true);
        ArrayList<StockItem> inventoryStockItems = (ArrayList<StockItem>) inventoryStockItemsField.get(inventory);
        inventoryStockItems.clear();
    }
}
