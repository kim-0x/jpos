package com.jpos.report.service.implementation;

import com.jpos.report.model.SaleDetail;
import com.jpos.report.model.SaleReport;
import com.jpos.report.model.SaleSummary;
import com.jpos.report.service.SaleGateway;
import com.jpos.report.service.SaleReportService;
import com.jpos.sale.model.ProductInfo;
import com.jpos.sale.model.ProductRef;
import com.jpos.sale.model.SaleItem;
import com.jpos.sale.service.InventoryGateway;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class SaleReportServiceImpl implements SaleReportService {
    private final SaleGateway saleGateway;
    private final InventoryGateway inventoryGateway;

    public SaleReportServiceImpl(SaleGateway saleGateway, InventoryGateway inventoryGateway) {
        this.saleGateway = saleGateway;
        this.inventoryGateway = inventoryGateway;
    }

    @Override
    public SaleReport getReport(Date fromDate, Date toDate) {
        var finalSaleSummary = saleGateway.getAllTransactions(fromDate, toDate)
                .map(st -> {
                    double revenue = st.getHeader().getGrandTotal();
                    double cost = Arrays.stream(st.getSaleItems())
                            .mapToDouble(in -> Math.abs(in.getCost() * in.getQuantity()))
                            .sum();
                    double profit = revenue - cost;
                    return new SaleSummary(revenue, profit, cost);
                })
                .reduce(new SaleSummary(0,0,0), SaleSummary::accumulate);

        var finalProductSummary = saleGateway.getAllTransactions(fromDate, toDate)
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
}
