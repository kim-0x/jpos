package com.jpos.sale.service;

import com.jpos.sale.model.SaleItem;
import com.jpos.sale.model.SaleTransaction;

import java.util.UUID;

public interface SaleTransactionService {
    /**
     * INTENT: Create a new sale transaction with the given receipt number.
     * PRECONDITION: receiptNumber is provided.
     * RETURNS: a new SaleTransaction object ready for adding items.
     * POSTCONDITION: a new transaction is created but not yet persisted.
     */
    SaleTransaction createTransaction(String receiptNumber);

    /**
     * INTENT: Add an item to an existing sale transaction.
     * PRECONDITION: transaction and saleItem are provided.
     * RETURNS: true when the item is successfully added; otherwise false.
     * POSTCONDITION: the item is added to the transaction and grand total is recalculated.
     */
    boolean addItemToTransaction(SaleTransaction transaction, SaleItem saleItem);

    /**
     * INTENT: Retrieve a sale transaction by its transaction ID.
     * PRECONDITION: transactionId is provided.
     * RETURNS: the SaleTransaction object if found; otherwise null.
     * POSTCONDITION: no repository data is modified.
     */
    SaleTransaction getTransactionById(UUID transactionId);

    /**
     * INTENT: Retrieve all sale transactions.
     * PRECONDITION: none.
     * RETURNS: an array of all SaleTransaction objects.
     * POSTCONDITION: no repository data is modified.
     */
    SaleTransaction[] getAllTransactions();

    /**
     * INTENT: Persist a completed sale transaction to the repository.
     * PRECONDITION: transaction is complete and ready to be saved.
     * RETURNS: true when the transaction is successfully persisted; otherwise false.
     * POSTCONDITION: the transaction and its items are saved to the repository.
     */
    boolean completeTransaction(SaleTransaction transaction);
}
