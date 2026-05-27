package com.jpos.sale.repository;

import com.jpos.sale.model.SaleItem;

import java.util.UUID;

public interface SaleItemRepository {
    abstract void add(SaleItem saleItem);
    abstract SaleItem[] getAll();
    abstract SaleItem[] getByTransactionId(UUID transactionId);
}
