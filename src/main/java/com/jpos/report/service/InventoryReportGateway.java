package com.jpos.report.service;

import com.jpos.report.model.StockDetail;

import java.util.Date;
import java.util.stream.Stream;

public interface InventoryReportGateway {
    Stream<StockDetail> getAllStockItems(Date fromDate, Date toDate);
}
