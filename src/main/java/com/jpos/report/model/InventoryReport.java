package com.jpos.report.model;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class InventoryReport {
    private final Date fromDate;
    private final Date toDate;
    private final double totalInventoryValue;

    private final Map<UUID, StockDetail> stockDetails;

    public InventoryReport(Date fromDate, Date toDate, double totalInventoryValue, Map<UUID, StockDetail> stockDetails) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.totalInventoryValue = totalInventoryValue;
        this.stockDetails = stockDetails;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public double getTotalInventoryValue() {
        return totalInventoryValue;
    }

    public Map<UUID, StockDetail> getStockDetails() {
        return stockDetails;
    }
}
