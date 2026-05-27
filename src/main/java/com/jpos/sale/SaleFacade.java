package com.jpos.sale;

import com.jpos.sale.exception.ProductNotFoundException;
import com.jpos.sale.model.PriceBook;
import com.jpos.sale.model.SaleItem;
import com.jpos.sale.model.SaleItemData;
import com.jpos.sale.model.SaleTransaction;
import com.jpos.sale.repository.PriceBookRepository;
import com.jpos.sale.repository.SaleHeaderRepository;
import com.jpos.sale.repository.SaleItemRepository;
import com.jpos.sale.service.ProductPriceService;
import com.jpos.sale.service.SaleTransactionService;
import com.jpos.sale.service.ProductCostProvider;
import com.jpos.sale.service.implementation.ProductPriceServiceImpl;
import com.jpos.sale.service.implementation.SaleTransactionServiceImpl;

import java.util.Objects;
import java.util.UUID;

public class SaleFacade {
    private final SaleTransactionService saleTransactionService;
    private final ProductPriceService productPriceService;

    public SaleFacade(SaleHeaderRepository saleHeaderRepository,
                      SaleItemRepository saleItemRepository,
                      PriceBookRepository priceBookRepository,
                      ProductCostProvider productCostProvider) {
        this.saleTransactionService = new SaleTransactionServiceImpl(saleHeaderRepository, saleItemRepository);
        this.productPriceService = new ProductPriceServiceImpl(priceBookRepository, productCostProvider);
    }

    /**
     * Process a complete sale transaction in a single operation.
     * This method creates a new transaction, adds all items, and completes the transaction.
     *
     * @param receiptNumber the receipt number for this transaction
     * @param items         array of SaleItemData records containing product information
     * @return the completed SaleTransaction
     */
    public SaleTransaction processSaleTransaction(String receiptNumber, SaleItemData[] items) {
        Objects.requireNonNull(receiptNumber, "receiptNumber must not be null");
        Objects.requireNonNull(items, "items must not be null");

        SaleTransaction transaction = saleTransactionService.createTransaction(receiptNumber);

        for (SaleItemData item : items) {
            SaleItem saleItem = new SaleItem(
                    item.getProductId(),
                    item.getQuantity(),
                    item.getPrice(),
                    transaction.getHeader().getTransactionId()
            );
            saleTransactionService.addItemToTransaction(transaction, saleItem);
        }

        saleTransactionService.completeTransaction(transaction);

        return transaction;
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
     * @param productId the product ID
     * @param margin    the profit margin
     * @throws ProductNotFoundException if the product is not found in inventory
     */
    public void setProductPrice(UUID productId, float margin) throws ProductNotFoundException {
        productPriceService.setProductPrice(productId, margin);
    }

    /**
     * Get the current price book entry for a product.
     *
     * @param productId the product ID
     * @return the PriceBook entry for the product or null if not found
     */
    public PriceBook getCurrentProductPrice(UUID productId) {
        return productPriceService.getCurrentProductPrice(productId);
    }
}