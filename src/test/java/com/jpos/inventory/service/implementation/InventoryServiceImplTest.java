package com.jpos.inventory.service.implementation;

import com.jpos.inventory.exception.ProductNotFoundException;
import com.jpos.inventory.model.Inventory;
import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.StockItem;
import com.jpos.inventory.model.StockRecord;
import com.jpos.inventory.repository.implementation.MockInventoryRepository;
import com.jpos.inventory.repository.implementation.MockProductRepository;
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

        inventoryService.entryStock("barcode-1", 4.5, 5);

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
        assertThrows(ProductNotFoundException.class,
                () -> inventoryService.entryStock("missing-barcode", 4.5, 5));
    }

    @Test
    public void shouldReturnStockReportForMultipleEntriesOfSameProduct() throws Exception {
        productRepository.saveProduct(createProduct("barcode-1", "Milk", "dairy"));
        productRepository.saveProduct(createProduct("barcode-2", "Coffee", "beverage"));

        inventoryService.entryStock("barcode-1", 1.5, 10);
        Thread.sleep(2);
        inventoryService.entryStock("barcode-1", 1.75, 20);
        inventoryService.entryStock("barcode-2", 5.5, 5);

        StockRecord[] stockReport = inventoryService.getStockReport();

        assertEquals(2, stockReport.length);

        StockRecord firstProductStock = getStockRecordByBarcode(stockReport, "barcode-1");
        assertNotNull(firstProductStock);
        assertEquals(1.75, firstProductStock.getCost(), 0.0);
        assertEquals(30.0, firstProductStock.getNumberInStock(), 0.0);

        StockRecord secondProductStock = getStockRecordByBarcode(stockReport, "barcode-2");
        assertNotNull(secondProductStock);
        assertEquals(5.5, secondProductStock.getCost(), 0.0);
        assertEquals(5.0, secondProductStock.getNumberInStock(), 0.0);
    }

    private Product createProduct(String barcode, String name, String category) {
        Product product = new Product();
        product.setBarcode(barcode);
        product.setName(name);
        product.setCategory(category);
        return product;
    }

    private StockRecord getStockRecordByBarcode(StockRecord[] stockRecords, String barcode) {
        for (StockRecord stockRecord : stockRecords) {
            if (stockRecord.getProduct().getBarcode().equals(barcode)) {
                return stockRecord;
            }
        }
        return null;
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
