package com.jpos.inventory.repository.implementation;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.model.StockItem;
import com.jpos.inventory.repository.implementation.file.CsvInventoryRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class FileInventoryRepositoryTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldLoadInventoryFromCsv() throws Exception {
        File inventoryFile = createFile("inventory.csv", """
                id,numberInStock,cost,productId,createdAt
                00000000-0000-0000-0000-002d156d2fae,50.0,1.0,00000000-0000-0000-0000-0050454d6969,2026-02-01 08:00:00.000000
                00000000-0000-0000-0000-0045ce9cdd30,-2.0,1.0,00000000-0000-0000-0000-0050454d6969,2026-02-27 22:11:17.660994
                """);

        CsvInventoryRepository repository = new CsvInventoryRepository(inventoryFile.toPath());
        UUID productId = repository.getStockItems()[0].getProductId();

        assertEquals(2, repository.getStockItems().length);
        assertEquals(48.0f, repository.getStockLevelOf(new ProductQuery(productId, null)), 0.0f);
        assertEquals(1.0, repository.getProductCost(new ProductQuery(productId, null)), 0.0);
    }

    @Test
    public void shouldPersistStockInToFile() throws Exception {
        File inventoryFile = createFile("inventory.csv", """
                id,numberInStock,cost,productId,createdAt
                """);

        CsvInventoryRepository repository = new CsvInventoryRepository(inventoryFile.toPath());
        StockItem stockItem = new StockItem();
        stockItem.setId(UUID.randomUUID());
        stockItem.setProductId(UUID.randomUUID());
        stockItem.setNumberInStock(5);
        stockItem.setCost(4.5);
        stockItem.setCreatedAt(new Date());

        repository.stockIn(stockItem);

        CsvInventoryRepository reloadedRepository = new CsvInventoryRepository(inventoryFile.toPath());
        assertEquals(1, reloadedRepository.getStockItems().length);
        assertEquals(5.0f, reloadedRepository.getStockItems()[0].getNumberInStock(), 0.0f);
    }

    @Test
    public void shouldThrowWhenInventoryFileDoesNotExist() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new CsvInventoryRepository(temporaryFolder.getRoot().toPath().resolve("missing-inventory.csv")));

        assertTrue(exception.getMessage().contains("does not exist"));
    }

    private File createFile(String fileName, String content) throws Exception {
        File file = temporaryFolder.newFile(fileName);
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        return file;
    }
}
