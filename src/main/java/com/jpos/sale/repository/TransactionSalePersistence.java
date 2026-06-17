package com.jpos.sale.repository;

import com.jpos.sale.model.SaleTransaction;

public interface TransactionSalePersistence {
    void addTransaction(SaleTransaction transaction);
}
