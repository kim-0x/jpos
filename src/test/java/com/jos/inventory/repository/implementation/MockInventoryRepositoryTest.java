package com.jos.inventory.repository.implementation;

import com.jos.inventory.model.Inventory;
import com.jos.inventory.model.ProductQuery;
import com.jos.inventory.model.StockItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class MockInventoryRepositoryTest {
    private MockInventoryRepository inventoryRepository;

    @Before
    public void setUp() throws Exception {
        resetInventory();
        inventoryRepository = new MockInventoryRepository();
    }

    @After
    public void tearDown() throws Exception {
        resetInventory();
    }

    @Test
    public void shouldReturnAllStockItems() {
        UUID firstProductId = UUID.randomUUID();
        UUID secondProductId = UUID.randomUUID();

        inventoryRepository.stockIn(createStockItem(firstProductId, 5, 4.5, new Date()));
        inventoryRepository.stockIn(createStockItem(firstProductId, 3, 5.0, new Date()));
        inventoryRepository.stockIn(createStockItem(secondProductId, 2, 6.0, new Date()));

        StockItem[] stockItems = inventoryRepository.getStockItems();

        assertEquals(3, stockItems.length);
    }

    @Test
    public void shouldDecreaseStockLevelAfterStockOut() {
        UUID productId = UUID.randomUUID();

        inventoryRepository.stockIn(createStockItem(productId, 10, 4.5, new Date()));
        inventoryRepository.stockOut(createStockItem(productId, 2, 0, new Date()));

        float stockLevel = inventoryRepository.getStockLevelOf(new ProductQuery(productId, null));

        assertEquals(8.0f, stockLevel, 0.0f);
    }

    @Test
    public void shouldReturnLatestProductCost() {
        UUID productId = UUID.randomUUID();

        inventoryRepository.stockIn(createStockItem(productId, 10, 4.5, monthsAgo(2)));
        inventoryRepository.stockIn(createStockItem(productId, 8, 5.5, monthsAgo(1)));
        inventoryRepository.stockIn(createStockItem(productId, 6, 6.5, monthsAgo(0)));

        double productCost = inventoryRepository.getProductCost(new ProductQuery(productId, null));

        assertEquals(6.5, productCost, 0.0);
    }

    @Test
    public void shouldThrowWhenStockOutMakesStockLowLevel() throws Exception {
        UUID productId = UUID.randomUUID();
        setLowStockLevel(3.0f);

        inventoryRepository.stockIn(createStockItem(productId, 5, 4.5, new Date()));

        assertThrows(IllegalStateException.class,
                () -> inventoryRepository.stockOut(createStockItem(productId, 2, 0, new Date())));
    }

    @Test
    public void shouldThrowWhenStockItemIsNull() {
        assertThrows(IllegalArgumentException.class, () -> inventoryRepository.stockIn(null));
        assertThrows(IllegalArgumentException.class, () -> inventoryRepository.stockOut(null));
    }

    @Test
    public void shouldThrowWhenStockItemProductIdIsNull() {
        StockItem stockItem = createStockItem(null, 1, 4.5, new Date());

        assertThrows(IllegalArgumentException.class, () -> inventoryRepository.stockIn(stockItem));
        assertThrows(IllegalArgumentException.class, () -> inventoryRepository.stockOut(stockItem));
    }

    @Test
    public void shouldThrowWhenProductQueryIsNull() {
        assertThrows(IllegalArgumentException.class, () -> inventoryRepository.getStockLevelOf(null));
        assertThrows(IllegalArgumentException.class, () -> inventoryRepository.getProductCost(null));
    }

    @Test
    public void shouldThrowWhenProductQueryProductIdIsNull() {
        ProductQuery productQuery = new ProductQuery(null, null);

        assertThrows(IllegalArgumentException.class, () -> inventoryRepository.getStockLevelOf(productQuery));
        assertThrows(IllegalArgumentException.class, () -> inventoryRepository.getProductCost(productQuery));
    }

    private StockItem createStockItem(UUID productId, float numberInStock, double cost, Date createdAt) {
        StockItem stockItem = new StockItem();
        stockItem.setId(productId);
        stockItem.setProductId(productId);
        stockItem.setNumberInStock(numberInStock);
        stockItem.setCost(cost);
        stockItem.setCreatedAt(createdAt);
        return stockItem;
    }

    private Date monthsAgo(int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -months);
        return calendar.getTime();
    }

    private void setLowStockLevel(float lowStockLevel) throws Exception {
        Field inventoryField = MockInventoryRepository.class.getDeclaredField("inventory");
        inventoryField.setAccessible(true);
        Inventory inventory = (Inventory) inventoryField.get(null);
        inventory.setLowStockLevel(lowStockLevel);
    }

    @SuppressWarnings("unchecked")
    private void resetInventory() throws Exception {
        Field inventoryField = MockInventoryRepository.class.getDeclaredField("inventory");
        inventoryField.setAccessible(true);
        Inventory inventory = (Inventory) inventoryField.get(null);
        inventory.setLowStockLevel(3.0f);

        Field inventoryStockItemsField = Inventory.class.getDeclaredField("stockItems");
        inventoryStockItemsField.setAccessible(true);
        ArrayList<StockItem> inventoryStockItems = (ArrayList<StockItem>) inventoryStockItemsField.get(inventory);
        inventoryStockItems.clear();
    }
}
