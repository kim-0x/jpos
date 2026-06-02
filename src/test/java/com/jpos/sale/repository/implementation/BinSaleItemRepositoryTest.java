package com.jpos.sale.repository.implementation;

import com.jpos.sale.model.SaleItem;
import com.jpos.sale.repository.implementation.file.BinSaleItemRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class BinSaleItemRepositoryTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldPersistAndReloadSaleItemCost() throws Exception {
        File saleItemFile = temporaryFolder.newFile("saleitem.dat");
        BinSaleItemRepository repository = new BinSaleItemRepository(saleItemFile.toPath());
        UUID transactionId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        repository.add(new SaleItem(productId, 2.0f, 3.5, 10.0, transactionId));

        BinSaleItemRepository reloadedRepository = new BinSaleItemRepository(saleItemFile.toPath());
        SaleItem[] reloadedItems = reloadedRepository.getByTransactionId(transactionId);

        assertEquals(1, reloadedItems.length);
        assertEquals(3.5, reloadedItems[0].getCost(), 0.0001);
        assertEquals(10.0, reloadedItems[0].getPrice(), 0.0001);
    }
}
