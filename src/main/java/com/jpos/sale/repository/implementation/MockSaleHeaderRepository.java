package com.jpos.sale.repository.implementation;

import com.jpos.sale.model.SaleHeader;
import com.jpos.sale.repository.SaleHeaderRepository;

import java.util.ArrayList;
import java.util.UUID;

public class MockSaleHeaderRepository implements SaleHeaderRepository {
    private final ArrayList<SaleHeader> headers = new ArrayList<>();

    @Override
    public void add(SaleHeader saleHeader) {
        headers.add(saleHeader);
    }

    @Override
    public SaleHeader[] getAll() {
        return headers.toArray(new SaleHeader[0]);
    }

    @Override
    public SaleHeader getById(UUID transactionId) {
        for (SaleHeader header : headers) {
            if (header.getTransactionId().equals(transactionId)) {
                return header;
            }
        }
        return null;
    }
}
