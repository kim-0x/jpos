package com.jpos.bluejay.api;

import com.jpos.report.ReportFacade;
import com.jpos.report.model.InventoryReport;
import com.jpos.report.model.SaleReport;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportFacade reportFacade;

    public ReportController(ReportFacade reportFacade) {
        this.reportFacade = reportFacade;
    }

    @GetMapping("/sales")
    public SaleReport getSalesReport(@RequestParam long fromEpochMs, @RequestParam long toEpochMs) {
        return reportFacade.getSaleReport(new Date(fromEpochMs), new Date(toEpochMs));
    }

    @GetMapping("/inventory")
    public InventoryReport getInventoryReport(@RequestParam long fromEpochMs, @RequestParam long toEpochMs) {
        return reportFacade.getInventoryReport(new Date(fromEpochMs), new Date(toEpochMs));
    }

    @PostMapping("/export")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void exportReports(@Valid @RequestBody ExportReportRequest request) {
        reportFacade.exportReports(new Date(request.fromEpochMs()), new Date(request.toEpochMs()));
    }

    public record ExportReportRequest(@NotNull Long fromEpochMs, @NotNull Long toEpochMs) {
    }
}
