package com.jpos.report;

import com.jpos.report.model.InventoryReport;
import com.jpos.report.model.SaleReport;
import com.jpos.report.service.InventoryReportService;
import com.jpos.report.service.ReportFilterService;
import com.jpos.report.service.SaleReportService;
import utils.DataSourcePathHelper;
import utils.JsonWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ReportFacade {
    private final SaleReportService saleReportService;
    private final InventoryReportService inventoryReportService;
    private final ReportFilterService reportFilterService;

    public ReportFacade(SaleReportService saleReportService,
                        InventoryReportService inventoryReportService,
                        ReportFilterService reportFilterService) {
        this.saleReportService = saleReportService;
        this.inventoryReportService = inventoryReportService;
        this.reportFilterService = reportFilterService;
    }

    public SaleReport getSaleReport(Date fromDate, Date toDate) {
        return this.saleReportService.getReport(fromDate, toDate);
    }

    public InventoryReport getInventoryReport(Date fromDate, Date toDate) {
        return this.inventoryReportService.getReport(fromDate, toDate);
    }

    public void exportReports(Date fromDate, Date toDate) {
        ExecutorService reportsThreadPool = Executors.newFixedThreadPool(2);
        String fromDateStr = JsonWriter.formatDate(fromDate);

        CompletableFuture<Void> saleReportTask = CompletableFuture.runAsync(() -> {
            var saleReport = this.saleReportService.getReport(fromDate, toDate);
            String jsonContent = this.saleReportService.toJson(saleReport);
            String filePath = String.format("sale-report__%s.json", fromDateStr);
            var resolvePath = DataSourcePathHelper.getDefaultFilePath("report/json", filePath);
            writeReportFile(jsonContent, resolvePath);
        }, reportsThreadPool);

        CompletableFuture<Void> inventoryReportTask = CompletableFuture.runAsync(() -> {
            var inventoryReport = this.inventoryReportService.getReport(fromDate, toDate);
            String jsonContent = this.inventoryReportService.toJson(inventoryReport);
            String filePath = String.format("inventory-report__%s.json", fromDateStr);
            var resolvePath = DataSourcePathHelper.getDefaultFilePath("report/json", filePath);
            writeReportFile(jsonContent, resolvePath);
        }, reportsThreadPool);

        CompletableFuture<Void> finalPipeline = CompletableFuture.allOf(saleReportTask, inventoryReportTask)
                .thenRunAsync(() -> {
                    String jsonContent = this.reportFilterService.addFilters(fromDate, "report/json");
                    var resolvePath = DataSourcePathHelper.getDefaultFilePath("report/json", "report-filter.json");
                    writeReportFile(jsonContent, resolvePath);
                });

        finalPipeline.join();

        reportsThreadPool.shutdown();
    }

    private void writeReportFile(String jsonContent, Path filePath) {
        try {
            Files.write(filePath, jsonContent.getBytes());
        } catch (Exception exception){
            throw new IllegalStateException(String.format("Unable to write json file: %s", filePath), exception);
        }
    }
}
