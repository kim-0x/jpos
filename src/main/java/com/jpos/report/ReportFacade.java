package com.jpos.report;

import com.jpos.report.model.InventoryReport;
import com.jpos.report.model.SaleReport;
import com.jpos.report.service.InventoryReportService;
import com.jpos.report.service.SaleReportService;
import utils.DataSourcePathHelper;
import utils.JsonWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public void exportReports(Date fromDate, Date toDate) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        String fromDateStr = JsonWriter.formatDate(fromDate);
        String toDateStr = JsonWriter.formatDate(toDate);

        Runnable saleReportRunnable = () -> {
            var saleReport = this.saleReportService.getReport(fromDate, toDate);
            String jsonContent = this.saleReportService.toJson(saleReport);
            String filePath = String.format("sale-report__%s__%s.json", fromDateStr, toDateStr);
            var resolvePath = DataSourcePathHelper.getDefaultFilePath("report/json", filePath);
            writeReportFile(jsonContent, resolvePath);
        };

        Runnable inventoryReportRunnable = () -> {
            var inventoryReport = this.inventoryReportService.getReport(fromDate, toDate);
            String jsonContent = this.inventoryReportService.toJson(inventoryReport);
            String filePath = String.format("inventory-report__%s__%s.json", fromDateStr, toDateStr);
            var resolvePath = DataSourcePathHelper.getDefaultFilePath("report/json", filePath);
            writeReportFile(jsonContent, resolvePath);
        };

        executorService.submit(saleReportRunnable);
        executorService.submit(inventoryReportRunnable);

        executorService.shutdown();
    }

    private void writeReportFile(String jsonContent, Path filePath) {
        try {
            Files.write(filePath, jsonContent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception exception){
            throw new IllegalStateException(String.format("Unable to write json file: %s", filePath), exception);
        }
    }
}
