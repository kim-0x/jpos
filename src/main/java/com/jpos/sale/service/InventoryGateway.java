package com.jpos.sale.service;

import com.jpos.inventory.model.StockItem;
import com.jpos.inventory.model.StockRecord;
import com.jpos.sale.model.ProductInfo;
import com.jpos.sale.model.ProductRef;

import java.util.Date;
import java.util.stream.Stream;

public interface InventoryGateway {
    ProductInfo findBy(ProductRef ref);
    void reduceStock(ProductRef ref, float numberOfStock);
    Stream<StockRecord> getAllStockOutTransaction(Date fromDate, Date toDate);
}