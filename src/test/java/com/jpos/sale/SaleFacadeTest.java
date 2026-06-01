package com.jpos.sale;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.sale.exception.ProductNotFoundException;
import com.jpos.sale.model.PriceBook;
import com.jpos.sale.model.SaleItemData;
import com.jpos.sale.model.SaleTransaction;
import com.jpos.sale.repository.implementation.MockPriceBookRepository;
import com.jpos.sale.repository.implementation.MockSaleHeaderRepository;
import com.jpos.sale.repository.implementation.MockSaleItemRepository;
import com.jpos.sale.service.implementation.MockInventoryGateway;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

public class SaleFacadeTest {
    private SaleFacade saleFacade;
    private MockInventoryGateway productCatalogGateway;

    private void registerPricedProduct(String barcode, UUID productId, double salePrice) {
        productCatalogGateway.registerProduct(barcode, productId, "Product-" + barcode, salePrice);
        saleFacade.setProductPrice(new ProductQuery(productId, barcode), 0.0f);
    }

    @Before
    public void setUp() {
        productCatalogGateway = new MockInventoryGateway();
        saleFacade = new SaleFacade(
                new MockSaleHeaderRepository(),
                new MockSaleItemRepository(),
                new MockPriceBookRepository(),
                productCatalogGateway
        );
    }

    // ---------------------------------------------------------------------------
    // processSaleTransaction
    // ---------------------------------------------------------------------------

    @Test
    public void shouldReturnCompletedTransactionWithCorrectGrandTotal() {
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        String barcode1 = "barcode-1";
        String barcode2 = "barcode-2";
        registerPricedProduct(barcode1, productId1, 10.0);
        registerPricedProduct(barcode2, productId2, 5.0);

        SaleItemData[] items = {
                new SaleItemData(barcode1, 2.0f),
                new SaleItemData(barcode2, 1.0f)
        };

        UUID transactionId = saleFacade.processSaleTransaction("REC-001", items);
        SaleTransaction result = saleFacade.getTransactionById(transactionId);

        assertNotNull(transactionId);
        assertNotNull(result);
        assertEquals("REC-001", result.getHeader().getReceiptNumber());
        assertEquals(2, result.getSaleItems().length);
        assertEquals(25.0, result.getHeader().getGrandTotal(), 0.0001);
    }

    @Test
    public void shouldMergeItemsWithSameProductIdIntoOneEntry() {
        UUID productId = UUID.randomUUID();
        String barcode = "barcode-3";
        registerPricedProduct(barcode, productId, 10.0);

        SaleItemData[] items = {
                new SaleItemData(barcode, 1.0f),
                new SaleItemData(barcode, 3.0f)
        };

        UUID transactionId = saleFacade.processSaleTransaction("REC-002", items);
        SaleTransaction result = saleFacade.getTransactionById(transactionId);

        assertNotNull(transactionId);
        assertNotNull(result);
        assertEquals(1, result.getSaleItems().length);
        assertEquals(4.0f, result.getSaleItems()[0].getQuantity(), 0.0001f);
        assertEquals(40.0, result.getHeader().getGrandTotal(), 0.0001);
    }

    @Test
    public void shouldProcessTransactionWithEmptyItemsAndZeroGrandTotal() {
        UUID transactionId = saleFacade.processSaleTransaction("REC-003", new SaleItemData[0]);
        SaleTransaction result = saleFacade.getTransactionById(transactionId);

        assertNotNull(transactionId);
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
        String barcode = "barcode-4";
        registerPricedProduct(barcode, productId, 20.0);

        UUID completedTransactionId = saleFacade.processSaleTransaction("REC-005",
                new SaleItemData[]{ new SaleItemData(barcode, 1.0f) });

        SaleTransaction found = saleFacade.getTransactionById(completedTransactionId);

        assertNotNull(found);
        assertEquals("REC-005", found.getHeader().getReceiptNumber());
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
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        String barcode1 = "barcode-5";
        String barcode2 = "barcode-6";
        registerPricedProduct(barcode1, productId1, 10.0);
        registerPricedProduct(barcode2, productId2, 5.0);

        saleFacade.processSaleTransaction("REC-006", new SaleItemData[]{
                new SaleItemData(barcode1, 1.0f)
        });
        saleFacade.processSaleTransaction("REC-007", new SaleItemData[]{
                new SaleItemData(barcode2, 2.0f)
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
        productCatalogGateway.registerProductCost(productId, 100.0);

        saleFacade.setProductPrice(new ProductQuery(productId, null), 0.25f);

        PriceBook priceBook = saleFacade.getCurrentProductPrice(new ProductQuery(productId, null));
        assertNotNull(priceBook);
        assertEquals(100.0, priceBook.getCost(), 0.0001);
        assertEquals(0.25f, priceBook.getMargin(), 0.0001f);
        assertEquals(125.0, priceBook.getSalePrice(), 0.0001);
    }

    @Test
    public void shouldThrowWhenSettingPriceForUnknownProduct() {
        UUID unknownProductId = UUID.randomUUID();

        assertThrows(ProductNotFoundException.class,
                () -> saleFacade.setProductPrice(new ProductQuery(unknownProductId, null), 0.2f));
    }

    @Test
    public void shouldSetAndGetProductPriceByBarcodeQuery() {
        UUID productId = UUID.randomUUID();
        String barcode = "barcode-1";
        productCatalogGateway.registerProduct(barcode, productId, "Product-barcode-1", 80.0);

        saleFacade.setProductPrice(new ProductQuery(null, barcode), 0.50f);

        PriceBook priceBook = saleFacade.getCurrentProductPrice(new ProductQuery(null, barcode));
        assertNotNull(priceBook);
        assertEquals(productId, priceBook.getProductId());
        assertEquals(120.0, priceBook.getSalePrice(), 0.0001);
    }

    @Test
    public void shouldReturnNullWhenNoProductPriceIsSet() {
        PriceBook priceBook = saleFacade.getCurrentProductPrice(new ProductQuery(UUID.randomUUID(), null));

        assertNull(priceBook);
    }

    @Test
    public void shouldReturnLatestPriceWhenProductPriceIsUpdatedMultipleTimes() throws InterruptedException {
        UUID productId = UUID.randomUUID();
        productCatalogGateway.registerProductCost(productId, 100.0);

        saleFacade.setProductPrice(new ProductQuery(productId, null), 0.10f);
        Thread.sleep(10); // ensure a time gap between entries
        productCatalogGateway.registerProductCost(productId, 120.0);
        saleFacade.setProductPrice(new ProductQuery(productId, null), 0.20f);

        PriceBook latest = saleFacade.getCurrentProductPrice(new ProductQuery(productId, null));
        assertNotNull(latest);
        assertEquals(120.0, latest.getCost(), 0.0001);
        assertEquals(144.0, latest.getSalePrice(), 0.0001);
    }

    @Test
    public void shouldGetProductNameByProductId() {
        UUID productId = UUID.randomUUID();
        productCatalogGateway.registerProduct("barcode-7", productId, "Coffee", 35.0);

        assertEquals("Coffee", saleFacade.getProductName(productId));
    }

    @Test
    public void shouldThrowWhenGettingProductNameForUnknownProductId() {
        assertThrows(ProductNotFoundException.class,
                () -> saleFacade.getProductName(UUID.randomUUID()));
    }
}
