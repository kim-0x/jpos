package com.jpos.inventory.service;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.inventory.model.StockRecord;

import java.util.Date;
import java.util.stream.Stream;

public interface InventoryService {
    abstract void entryStock(String barcode, double cost, float numberInStock);
    abstract void reduceStock(ProductQuery productQuery, float numberOfStock);
    abstract StockRecord[] getStockReport();
    abstract Stream<StockRecord> getAllStockTransactions(Date fromDate, Date toDate);
    abstract double getProductCostBy(ProductQuery productQuery);
}
