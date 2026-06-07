package com.jpos.report.service.implementation;

import com.jpos.inventory.service.InventoryService;
import com.jpos.report.model.StockDetail;
import com.jpos.report.service.InventoryReportGateway;

import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

public class InventoryReportGatewayImpl implements InventoryReportGateway {
    private final InventoryService inventoryService;

    public  InventoryReportGatewayImpl(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public Stream<StockDetail> getAllStockItems(Date fromDate, Date toDate) {
        return inventoryService.getAllStockTransactions(fromDate, toDate)
                .map(sr -> {
                    UUID productId = sr.getProduct().getId();
                    String productName = sr.getProduct().getName();

                    return new StockDetail(
                            productId,
                            productName,
                            sr.getCost(),
                            (float) sr.getNumberInStock(),
                            sr.getCreatedAt()
                    );
                });
    }
}
