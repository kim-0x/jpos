package com.jpos.sale.model;

import java.util.Arrays;
import java.util.Objects;

public class SaleTransaction {
    private final SaleHeader header;

    private SaleItem[] saleItems = new SaleItem[0];

    public SaleHeader getHeader() {
        return header;
    }

    public SaleTransaction(SaleHeader header) {
        this.header = Objects.requireNonNull(header, "header must not be null");
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
                        header.getTransactionId()
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
        header.setGrandTotal(Arrays.stream(saleItems)
                .mapToDouble(SaleItem::getTotalPrice)
                .sum());
    }
}
