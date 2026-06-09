package com.jpos.report.service.implementation;

import com.jpos.report.model.InventoryReport;
import com.jpos.report.model.StockDetail;
import com.jpos.report.service.InventoryReportGateway;
import com.jpos.report.service.InventoryReportService;

import java.util.Date;
import java.util.stream.Collectors;

public class InventoryReportServiceImpl implements InventoryReportService {
    private final InventoryReportGateway inventoryReportGateway;

    public InventoryReportServiceImpl(InventoryReportGateway inventoryReportGateway) {
        this.inventoryReportGateway = inventoryReportGateway;
    }

    @Override
    public InventoryReport getReport(Date fromDate, Date toDate) {
        var totalInventoryValue = inventoryReportGateway.getAllStockItems(fromDate, toDate)
                .mapToDouble(StockDetail::getTotalStockValue)
                .sum();

        var finalStockDetails = inventoryReportGateway.getAllStockItems(fromDate, toDate)
                .collect(Collectors.toMap(
                        StockDetail::getProductId,
                        si -> si,
                        StockDetail::accumulate
                ));

        return new InventoryReport(fromDate, toDate, totalInventoryValue, finalStockDetails);
    }

    @Override
    public String toJson(InventoryReport inventoryReport) {
        return "";
    }
}
