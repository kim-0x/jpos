package com.jpos.sale.repository.implementation;

import com.jpos.sale.model.SaleItem;
import com.jpos.sale.repository.SaleItemRepository;

import java.util.ArrayList;
import java.util.UUID;

public class MockSaleItemRepository implements SaleItemRepository {
    private final ArrayList<SaleItem> saleItems = new ArrayList<>();

    @Override
    public void add(SaleItem saleItem) {
        saleItems.add(saleItem);
    }

    @Override
    public SaleItem[] getAll() {
        return saleItems.toArray(new SaleItem[0]);
    }

    @Override
    public SaleItem[] getByTransactionId(UUID transactionId) {
        ArrayList<SaleItem> matched = new ArrayList<>();
        for (SaleItem item : saleItems) {
            if (item.getTransactionId().equals(transactionId)) {
                matched.add(item);
            }
        }
        return matched.toArray(new SaleItem[0]);
    }
}
