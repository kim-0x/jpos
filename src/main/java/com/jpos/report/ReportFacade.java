package com.jpos.report;

import com.jpos.report.model.InventoryReport;
import com.jpos.report.model.SaleReport;
import com.jpos.report.service.InventoryReportService;
import com.jpos.report.service.SaleReportService;

import java.util.Date;

public class ReportFacade {
    private final SaleReportService saleReportService;
    private final InventoryReportService inventoryReportService;

    public ReportFacade(SaleReportService saleReportService, InventoryReportService inventoryReportService) {
        this.saleReportService = saleReportService;
        this.inventoryReportService = inventoryReportService;
    }

    public SaleReport getSaleReport(Date fromDate, Date toDate) {
        return this.saleReportService.getReport(fromDate, toDate);
    }

    public InventoryReport getInventoryReport(Date fromDate, Date toDate) {
        return this.inventoryReportService.getReport(fromDate, toDate);
    }
}
