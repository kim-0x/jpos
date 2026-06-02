package com.jpos.sale.repository.implementation;

import com.jpos.sale.model.SaleItem;
import com.jpos.sale.repository.implementation.file.CsvSaleItemRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class CsvSaleItemRepositoryTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldPersistAndReloadSaleItemCost() throws Exception {
        File saleItemFile = createEmptySaleItemFile("saleitem.csv");
        CsvSaleItemRepository repository = new CsvSaleItemRepository(saleItemFile.toPath());
        UUID transactionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        repository.add(new SaleItem(productId, 2.0f, 3.5, 10.0, transactionId));

        CsvSaleItemRepository reloadedRepository = new CsvSaleItemRepository(saleItemFile.toPath());
        SaleItem[] reloadedItems = reloadedRepository.getByTransactionId(transactionId);

        assertEquals(1, reloadedItems.length);
        assertEquals(3.5, reloadedItems[0].getCost(), 0.0001);
        assertEquals(10.0, reloadedItems[0].getPrice(), 0.0001);
    }

    private File createEmptySaleItemFile(String fileName) throws Exception {
        File file = temporaryFolder.newFile(fileName);
        Files.writeString(file.toPath(), "productId,quantity,cost,price,transactionId\n", StandardCharsets.UTF_8);
        return file;
    }
}
