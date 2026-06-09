package com.jpos.report.service.implementation;

import com.jpos.report.model.SaleDetail;
import com.jpos.report.model.SaleReport;
import com.jpos.report.model.SaleSummary;
import com.jpos.report.service.SaleReportGateway;
import com.jpos.report.service.SaleReportService;
import com.jpos.sale.model.ProductInfo;
import com.jpos.sale.model.ProductRef;
import com.jpos.sale.model.SaleItem;
import com.jpos.sale.service.InventoryGateway;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class SaleReportServiceImpl implements SaleReportService {
    private final SaleReportGateway saleReportGateway;
    private final InventoryGateway inventoryGateway;

    public SaleReportServiceImpl(SaleReportGateway saleReportGateway, InventoryGateway inventoryGateway) {
        this.saleReportGateway = saleReportGateway;
        this.inventoryGateway = inventoryGateway;
    }

    @Override
    public SaleReport getReport(Date fromDate, Date toDate) {
        var finalSaleSummary = saleReportGateway.getAllTransactions(fromDate, toDate)
                .map(st -> {
                    double revenue = st.getHeader().getGrandTotal();
                    double cost = Arrays.stream(st.getSaleItems())
                            .mapToDouble(in -> Math.abs(in.getCost() * in.getQuantity()))
                            .sum();
                    double profit = revenue - cost;
                    return new SaleSummary(revenue, profit, cost);
                })
                .reduce(new SaleSummary(0,0,0), SaleSummary::accumulate);

        var finalProductSummary = saleReportGateway.getAllTransactions(fromDate, toDate)
                .flatMap(st -> Arrays.stream(st.getSaleItems()))
                .collect(Collectors.toMap(
                        SaleItem::getProductId,
                        si -> {
                            ProductRef productRef = new ProductRef(si.getProductId(), null);
                            ProductInfo productInfo = inventoryGateway.findBy(productRef);
    
                            return new SaleDetail(
                                    productInfo.name(),
                                    si.getQuantity(),
                                    si.getCost(),
                                    si.getTotalPrice());
                        },
                        SaleDetail::accumulate
                ));

        return new SaleReport(fromDate, toDate, finalSaleSummary, finalProductSummary);
    }

    @Override
    public String toJson(SaleReport saleReport) {
        return "";
    }
}
