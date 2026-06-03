package com.jpos.report.model;

import java.util.*;

public class SaleReport {
    private final Date fromDate;
    private final Date toDate;
    private final SaleSummary saleSummary;
    private final Map<UUID, SaleDetail> saleDetails;

    public SaleReport(Date  fromDate, Date toDate, SaleSummary saleSummary,  Map<UUID, SaleDetail> saleDetails) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.saleSummary = saleSummary;
        this.saleDetails = saleDetails;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public SaleSummary getSaleSummary() {
        return saleSummary;
    }

    public Map<UUID, SaleDetail> getSaleDetails() {
        return saleDetails;
    }
}
