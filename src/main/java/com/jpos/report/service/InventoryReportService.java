package com.jpos.report.service;

import com.jpos.report.model.InventoryReport;

import java.util.Date;

public interface InventoryReportService {
    InventoryReport getReport(Date fromDate, Date toDate);
    String toJson(InventoryReport inventoryReport);
}
