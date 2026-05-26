package com.jpos.sale.model;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class SaleTransaction {
    private final UUID transactionId;
    private final String receiptNumber;
    private double grandTotal;
    private final Date transactionDate;

    private SaleItem[] saleItems = new SaleItem[0];

    public UUID getTransactionId() {
        return transactionId;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public SaleTransaction(UUID transactionId, String receiptNumber, double grandTotal, Date transactionDate) {
        this.transactionId = transactionId;
        this.receiptNumber = receiptNumber;
        this.grandTotal = grandTotal;
        this.transactionDate = transactionDate;
    }

    public SaleItem[] getSaleItems() {
        return saleItems;
    }

    public void addItem(SaleItem saleItem) {
        Objects.requireNonNull(saleItem, "saleItem must not be null");

        for (int i = 0; i < saleItems.length; i++) {
            SaleItem existing = saleItems[i];
            if (existing.getProductId().equals(saleItem.getProductId())) {
                float mergedQuantity = existing.getQuantity() + saleItem.getQuantity();
                saleItems[i] = new SaleItem(
                        existing.getProductId(),
                        mergedQuantity,
                        existing.getPrice(),
                        transactionId
                );
                recalculateGrandTotal();
                return;
            }
        }

        SaleItem[] grown = Arrays.copyOf(saleItems, saleItems.length + 1);
        grown[grown.length - 1] = saleItem;
        saleItems = grown;
        recalculateGrandTotal();
    }

    private void recalculateGrandTotal() {
        grandTotal = Arrays.stream(saleItems)
                .mapToDouble(SaleItem::getTotalPrice)
                .sum();
    }
}
