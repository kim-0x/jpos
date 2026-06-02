package com.jpos.report.model;

import java.util.*;

public class SaleReport {
    private Date fromDate;
    private Date toDate;
    private SaleSummary saleSummary;

    private Map<UUID, SaleDetail> saleDetails;

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public SaleSummary getSaleSummary() {
        return saleSummary;
    }

    public void setSaleSummary(SaleSummary saleSummary) {
        this.saleSummary = saleSummary;
    }

    public Map<UUID, SaleDetail> getSaleDetails() {
        return saleDetails;
    }

    public void setSaleDetails(Map<UUID, SaleDetail> saleDetails) {
        this.saleDetails = saleDetails;
    }
}
