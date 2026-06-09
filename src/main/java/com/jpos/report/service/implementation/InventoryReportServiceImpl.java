package com.jpos.report.service.implementation;

import com.jpos.report.exception.InvalidInventoryReportException;
import com.jpos.report.model.InventoryReport;
import com.jpos.report.model.StockDetail;
import com.jpos.report.service.InventoryReportGateway;
import com.jpos.report.service.InventoryReportService;
import utils.JsonWriter;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InventoryReportServiceImpl implements InventoryReportService {
    private final InventoryReportGateway inventoryReportGateway;

    public InventoryReportServiceImpl(InventoryReportGateway inventoryReportGateway) {
        this.inventoryReportGateway = inventoryReportGateway;
    }

    @Override
    public InventoryReport getReport(Date fromDate, Date toDate) {
        var totalInventoryValue = inventoryReportGateway.getAllStockItems(fromDate, toDate)
                .mapToDouble(StockDetail::getTotalStockValue)
                .sum();

        var finalStockDetails = inventoryReportGateway.getAllStockItems(fromDate, toDate)
                .collect(Collectors.toMap(
                        StockDetail::getProductId,
                        si -> si,
                        StockDetail::accumulate
                ));

        return new InventoryReport(fromDate, toDate, totalInventoryValue, finalStockDetails);
    }

    @Override
    public String toJson(InventoryReport inventoryReport) {
        validateInventoryReport(inventoryReport);

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"fromDate\":").append(JsonWriter.toJsonString(JsonWriter.formatDate(inventoryReport.getFromDate()))).append(',');
        jsonBuilder.append("\"toDate\":").append(JsonWriter.toJsonString(JsonWriter.formatDate(inventoryReport.getToDate()))).append(',');
        jsonBuilder.append("\"totalInventoryValue\":").append(inventoryReport.getTotalInventoryValue()).append(',');
        jsonBuilder.append("\"items\":[");

        AtomicInteger itemIndex = new AtomicInteger();
        inventoryReport.getStockDetails().values().stream()
                .filter(Objects::nonNull)
                .forEach(item -> {
                    if (itemIndex.getAndIncrement() > 0) {
                        jsonBuilder.append(',');
                    }

                    jsonBuilder.append('{');
                    jsonBuilder.append("\"productId\":").append(JsonWriter.toJsonString(item.getProductId() == null ? null : item.getProductId().toString())).append(',');
                    jsonBuilder.append("\"name\":").append(JsonWriter.toJsonString(item.getProductName())).append(',');
                    jsonBuilder.append("\"qty\":").append(item.getTotalNumberInStock()).append(',');
                    jsonBuilder.append("\"status\":").append(JsonWriter.toJsonString(item.getReorderStatus().getValue())).append(',');
                    jsonBuilder.append("\"cost\":").append(item.getLatestCost()).append(',');
                    jsonBuilder.append("\"value\":").append(item.getTotalStockValue());
                    jsonBuilder.append('}');
                });

        jsonBuilder.append(']');
        jsonBuilder.append('}');
        return jsonBuilder.toString();
    }

    private void validateInventoryReport(InventoryReport inventoryReport) {
        if (inventoryReport == null) {
            throw new InvalidInventoryReportException("Inventory report must not be null.");
        }
        if (inventoryReport.getStockDetails() == null) {
            throw new InvalidInventoryReportException("Inventory report details must not be null.");
        }
    }
}
