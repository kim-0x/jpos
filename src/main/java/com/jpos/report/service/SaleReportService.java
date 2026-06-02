package com.jpos.report.service;

import com.jpos.report.model.SaleReport;

import java.util.Date;

public interface SaleReportService {
    SaleReport getReport(Date fromDate, Date toDate);
}
