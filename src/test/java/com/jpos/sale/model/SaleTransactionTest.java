package com.jpos.sale.model;

import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class SaleTransactionTest {

    @Test
    public void shouldAddUniqueItemsAndRecalculateGrandTotal() {
        SaleTransaction transaction = new SaleTransaction(UUID.randomUUID(), "R-001", 0.0, new Date());

        transaction.addItem(new SaleItem(UUID.randomUUID(), 2.0f, 10.0, transaction.getTransactionId()));
        transaction.addItem(new SaleItem(UUID.randomUUID(), 1.0f, 5.0, transaction.getTransactionId()));

        assertEquals(2, transaction.getSaleItems().length);
        assertEquals(25.0, transaction.getGrandTotal(), 0.0001);
    }

    @Test
    public void shouldMergeDuplicateProductIdAndIncreaseQuantity() {
        SaleTransaction transaction = new SaleTransaction(UUID.randomUUID(), "R-002", 0.0, new Date());
        UUID productId = UUID.randomUUID();

        transaction.addItem(new SaleItem(productId, 1.0f, 10.0, transaction.getTransactionId()));
        transaction.addItem(new SaleItem(productId, 3.0f, 10.0, transaction.getTransactionId()));

        assertEquals(1, transaction.getSaleItems().length);
        assertEquals(4.0f, transaction.getSaleItems()[0].getQuantity(), 0.0001f);
        assertEquals(40.0, transaction.getGrandTotal(), 0.0001);
    }
}
