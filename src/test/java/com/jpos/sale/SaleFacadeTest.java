package com.jpos.sale;

import com.jpos.sale.exception.ProductNotFoundException;
import com.jpos.sale.model.PriceBook;
import com.jpos.sale.model.SaleItemData;
import com.jpos.sale.model.SaleTransaction;
import com.jpos.sale.repository.implementation.MockPriceBookRepository;
import com.jpos.sale.repository.implementation.MockSaleHeaderRepository;
import com.jpos.sale.repository.implementation.MockSaleItemRepository;
import com.jpos.sale.service.implementation.MockProductCostProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

public class SaleFacadeTest {
    private SaleFacade saleFacade;
    private MockProductCostProvider costProvider;

    @Before
    public void setUp() {
        costProvider = new MockProductCostProvider();
        saleFacade = new SaleFacade(
                new MockSaleHeaderRepository(),
                new MockSaleItemRepository(),
                new MockPriceBookRepository(),
                costProvider
        );
    }

    // ---------------------------------------------------------------------------
    // processSaleTransaction
    // ---------------------------------------------------------------------------

    @Test
    public void shouldReturnCompletedTransactionWithCorrectGrandTotal() {
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        SaleItemData[] items = {
                new SaleItemData(productId1, 2.0f, 10.0),
                new SaleItemData(productId2, 1.0f, 5.0)
        };

        SaleTransaction result = saleFacade.processSaleTransaction("REC-001", items);

        assertNotNull(result);
        assertEquals("REC-001", result.getHeader().getReceiptNumber());
        assertEquals(2, result.getSaleItems().length);
        assertEquals(25.0, result.getHeader().getGrandTotal(), 0.0001);
    }

    @Test
    public void shouldMergeItemsWithSameProductIdIntoOneEntry() {
        UUID productId = UUID.randomUUID();
        SaleItemData[] items = {
                new SaleItemData(productId, 1.0f, 10.0),
                new SaleItemData(productId, 3.0f, 10.0)
        };

        SaleTransaction result = saleFacade.processSaleTransaction("REC-002", items);

        assertEquals(1, result.getSaleItems().length);
        assertEquals(4.0f, result.getSaleItems()[0].getQuantity(), 0.0001f);
        assertEquals(40.0, result.getHeader().getGrandTotal(), 0.0001);
    }

    @Test
    public void shouldProcessTransactionWithEmptyItemsAndZeroGrandTotal() {
        SaleTransaction result = saleFacade.processSaleTransaction("REC-003", new SaleItemData[0]);

        assertNotNull(result);
        assertEquals(0, result.getSaleItems().length);
        assertEquals(0.0, result.getHeader().getGrandTotal(), 0.0001);
    }

    @Test
    public void shouldThrowWhenReceiptNumberIsNull() {
        assertThrows(NullPointerException.class,
                () -> saleFacade.processSaleTransaction(null, new SaleItemData[0]));
    }

    @Test
    public void shouldThrowWhenItemsArrayIsNull() {
        assertThrows(NullPointerException.class,
                () -> saleFacade.processSaleTransaction("REC-004", null));
    }

    // ---------------------------------------------------------------------------
    // getTransactionById
    // ---------------------------------------------------------------------------

    @Test
    public void shouldRetrieveCompletedTransactionByItsId() {
        UUID productId = UUID.randomUUID();
        SaleTransaction completed = saleFacade.processSaleTransaction("REC-005",
                new SaleItemData[]{ new SaleItemData(productId, 1.0f, 20.0) });

        SaleTransaction found = saleFacade.getTransactionById(completed.getHeader().getTransactionId());

        assertNotNull(found);
        assertEquals(completed.getHeader().getReceiptNumber(), found.getHeader().getReceiptNumber());
        assertEquals(20.0, found.getHeader().getGrandTotal(), 0.0001);
    }

    @Test
    public void shouldReturnNullWhenTransactionIdDoesNotExist() {
        SaleTransaction result = saleFacade.getTransactionById(UUID.randomUUID());

        assertNull(result);
    }

    // ---------------------------------------------------------------------------
    // getAllTransactions
    // ---------------------------------------------------------------------------

    @Test
    public void shouldReturnAllCompletedTransactions() {
        saleFacade.processSaleTransaction("REC-006", new SaleItemData[]{
                new SaleItemData(UUID.randomUUID(), 1.0f, 10.0)
        });
        saleFacade.processSaleTransaction("REC-007", new SaleItemData[]{
                new SaleItemData(UUID.randomUUID(), 2.0f, 5.0)
        });

        SaleTransaction[] all = saleFacade.getAllTransactions();

        assertEquals(2, all.length);
    }

    @Test
    public void shouldReturnEmptyArrayWhenNoTransactionsExist() {
        SaleTransaction[] all = saleFacade.getAllTransactions();

        assertEquals(0, all.length);
    }

    // ---------------------------------------------------------------------------
    // setProductPrice / getCurrentProductPrice
    // ---------------------------------------------------------------------------

    @Test
    public void shouldSetProductPriceAndCalculateSalePriceFromMargin() {
        UUID productId = UUID.randomUUID();
        costProvider.registerProductCost(productId, 100.0);

        saleFacade.setProductPrice(productId, 0.25f);

        PriceBook priceBook = saleFacade.getCurrentProductPrice(productId);
        assertNotNull(priceBook);
        assertEquals(100.0, priceBook.getCost(), 0.0001);
        assertEquals(0.25f, priceBook.getMargin(), 0.0001f);
        assertEquals(125.0, priceBook.getSalePrice(), 0.0001);
    }

    @Test
    public void shouldThrowWhenSettingPriceForUnknownProduct() {
        UUID unknownProductId = UUID.randomUUID();

        assertThrows(ProductNotFoundException.class,
                () -> saleFacade.setProductPrice(unknownProductId, 0.2f));
    }

    @Test
    public void shouldReturnNullWhenNoProductPriceIsSet() {
        PriceBook priceBook = saleFacade.getCurrentProductPrice(UUID.randomUUID());

        assertNull(priceBook);
    }

    @Test
    public void shouldReturnLatestPriceWhenProductPriceIsUpdatedMultipleTimes() throws InterruptedException {
        UUID productId = UUID.randomUUID();
        costProvider.registerProductCost(productId, 100.0);

        saleFacade.setProductPrice(productId, 0.10f);
        Thread.sleep(10); // ensure a time gap between entries
        costProvider.registerProductCost(productId, 120.0);
        saleFacade.setProductPrice(productId, 0.20f);

        PriceBook latest = saleFacade.getCurrentProductPrice(productId);
        assertNotNull(latest);
        assertEquals(120.0, latest.getCost(), 0.0001);
        assertEquals(144.0, latest.getSalePrice(), 0.0001);
    }
}
