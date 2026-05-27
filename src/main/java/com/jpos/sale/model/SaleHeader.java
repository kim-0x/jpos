package com.jpos.sale.model;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class SaleHeader {
    private final UUID transactionId;
    private final String receiptNumber;
    private double grandTotal;
    private final Date transactionDate;

    public SaleHeader(UUID transactionId, String receiptNumber, double grandTotal, Date transactionDate) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId must not be null");
        this.receiptNumber = Objects.requireNonNull(receiptNumber, "receiptNumber must not be null");
        this.grandTotal = grandTotal;
        this.transactionDate = Objects.requireNonNull(transactionDate, "transactionDate must not be null");
    }

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

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }
}
