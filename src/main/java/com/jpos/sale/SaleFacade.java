package com.jpos.sale;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.sale.exception.ProductNotFoundException;
import com.jpos.sale.model.PriceBook;
import com.jpos.sale.model.ProductRef;
import com.jpos.sale.model.SaleItem;
import com.jpos.sale.model.SaleItemData;
import com.jpos.sale.model.SaleTransaction;
import com.jpos.sale.repository.PriceBookRepository;
import com.jpos.sale.repository.SaleHeaderRepository;
import com.jpos.sale.repository.SaleItemRepository;
import com.jpos.sale.service.InventoryGateway;
import com.jpos.sale.service.ProductPriceService;
import com.jpos.sale.service.SaleTransactionService;
import com.jpos.sale.service.implementation.ProductPriceServiceImpl;
import com.jpos.sale.service.implementation.SaleTransactionServiceImpl;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class SaleFacade {
    private final SaleTransactionService saleTransactionService;
    private final ProductPriceService productPriceService;
    private final InventoryGateway inventoryGateway;

    public SaleFacade(SaleHeaderRepository saleHeaderRepository,
                      SaleItemRepository saleItemRepository,
                      PriceBookRepository priceBookRepository,
                      InventoryGateway inventoryGateway) {
        this.saleTransactionService = new SaleTransactionServiceImpl(saleHeaderRepository, saleItemRepository);
        this.productPriceService = new ProductPriceServiceImpl(priceBookRepository, inventoryGateway);
        this.inventoryGateway = inventoryGateway;
    }

    /**
     * Process a complete sale transaction in a single operation.
     * This method creates a new transaction, adds all items, and completes the transaction.
     *
     * @param receiptNumber the receipt number for this transaction
     * @param items         array of SaleItemData records containing product information
     * @return the transaction ID of the completed SaleTransaction
     */
    public UUID processSaleTransaction(String receiptNumber, SaleItemData[] items) {
        return processSaleTransaction(receiptNumber, items, new Date());
    }

    public UUID processSaleTransaction(String receiptNumber, SaleItemData[] items, Date transactionDate) {
        Objects.requireNonNull(receiptNumber, "receiptNumber must not be null");
        Objects.requireNonNull(items, "items must not be null");
        Objects.requireNonNull(transactionDate, "transactionDate must not be null");

        SaleTransaction transaction = saleTransactionService.createTransaction(receiptNumber, transactionDate);

        for (SaleItemData item : items) {
            var productRef = new ProductRef(null, item.getBarcode());
            var productInfo = inventoryGateway.findBy(productRef);
            var productId = productInfo.productId();
            var priceBook = getCurrentProductPrice(new ProductQuery(productId, productInfo.barcode()));

            SaleItem saleItem = new SaleItem(
                    productId,
                    item.getQuantity(),
                    productInfo.cost(),
                    priceBook.getSalePrice(),
                    transaction.getHeader().getTransactionId()
            );
            saleTransactionService.addItemToTransaction(transaction, saleItem);
            inventoryGateway.reduceStock(productRef, item.getQuantity());
        }

        saleTransactionService.completeTransaction(transaction);

        return transaction.getHeader().getTransactionId();
    }

    /**
     * Get a transaction by its ID.
     *
     * @param transactionId the transaction ID
     * @return the SaleTransaction or null if not found
     */
    public SaleTransaction getTransactionById(UUID transactionId) {
        return saleTransactionService.getTransactionById(transactionId);
    }

    /**
     * Get all sale transactions.
     *
     * @return array of all SaleTransaction objects
     */
    public SaleTransaction[] getAllTransactions() {
        return saleTransactionService.getAllTransactions();
    }

    /**
     * Set the price for a product with a given margin.
     *
     * @param productQuery query containing product identifiers (id and/or barcode)
     * @param margin    the profit margin
     * @throws ProductNotFoundException if the product is not found in inventory
     */
    public void setProductPrice(ProductQuery productQuery, float margin) throws ProductNotFoundException {
        productPriceService.setProductPrice(productQuery, margin);
    }

    /**
     * Get the current price book entry for a product.
     *
     * @param productQuery query containing product identifiers (id and/or barcode)
     * @return the PriceBook entry for the product or null if not found
     */
    public PriceBook getCurrentProductPrice(ProductQuery productQuery) {
        try {
            return productPriceService.getCurrentProductPrice(productQuery);
        } catch (ProductNotFoundException e) {
            return null;
        }
    }

    /**
     * Validate if product with barcode is available in inventory
     *
     * @param barcode product barcode
     * @return true if product is available to buy. Otherwise, false
     */
    public boolean isProductAvailable(String barcode) {
        try {
            var productInfo = inventoryGateway.findBy(new ProductRef(null, barcode));
            return productInfo.cost() != 0;
        } catch (ProductNotFoundException e) {
            return false;
        }
    }

    public String getProductName(UUID productId) {
        return inventoryGateway.findBy(new ProductRef(productId, null)).name();
    }

}
