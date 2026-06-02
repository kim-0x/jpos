package com.jpos.report.service.implementation;

import com.jpos.report.model.SaleReport;
import com.jpos.report.service.SaleGateway;
import com.jpos.report.service.SaleReportService;
import com.jpos.sale.service.InventoryGateway;

import java.util.Date;

public class SaleReportServiceImpl implements SaleReportService {
    private final SaleGateway saleGateway;
    private final InventoryGateway inventoryGateway;

    public SaleReportServiceImpl(SaleGateway saleGateway, InventoryGateway inventoryGateway) {
        this.saleGateway = saleGateway;
        this.inventoryGateway = inventoryGateway;
    }

    @Override
    public SaleReport getReport(Date fromDate, Date toDate) {
        var totalRevenue = saleGateway.getAllTransactions(fromDate, toDate)
                .mapToDouble(st -> st.getHeader().getGrandTotal())
                .sum();

        var totalCost = inventoryGateway.getAllStockOutTransaction(fromDate, toDate)
                .mapToDouble(in -> Math.abs(in.getCost() * in.getNumberInStock()))
                .sum();

        SaleReport report = new SaleReport();
        report.setFromDate(fromDate);
        report.setToDate(toDate);
        report.setTotalRevenue(totalRevenue);
        report.setTotalCost(totalCost);
        report.setTotalProfit(totalRevenue - totalCost);

        return report;
    }
}
