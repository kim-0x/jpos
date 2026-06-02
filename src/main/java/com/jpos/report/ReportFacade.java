package com.jpos.report;

import com.jpos.report.model.SaleReport;
import com.jpos.report.service.SaleReportService;

import java.util.Date;

public class ReportFacade {
    private final SaleReportService saleReportService;

    public ReportFacade(SaleReportService saleReportService) {
        this.saleReportService = saleReportService;
    }

    public SaleReport getSaleReport(Date fromDate, Date toDate) {
        return this.saleReportService.getReport(fromDate, toDate);
    }
}
