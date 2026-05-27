package com.jpos.sale.repository;

import com.jpos.sale.model.SaleHeader;

import java.util.UUID;

public interface SaleHeaderRepository {
    abstract void add(SaleHeader saleHeader);
    abstract SaleHeader[] getAll();
    abstract SaleHeader getById(UUID transactionId);
}
