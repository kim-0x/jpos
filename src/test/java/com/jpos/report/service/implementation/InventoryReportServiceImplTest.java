package com.jpos.report.service.implementation;

import com.jpos.report.exception.InvalidInventoryReportException;
import com.jpos.report.model.InventoryReport;
import com.jpos.report.model.StockDetail;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InventoryReportServiceImplTest {
    private static final UUID PRODUCT_ID_1 = UUID.randomUUID();
    private static final UUID PRODUCT_ID_2 = UUID.randomUUID();

    private Date fromDate;
    private Date toDate;

    @Before
    public void setUp() {
        fromDate = new Date(0);
        toDate = new Date();
    }

    @Test
    public void shouldAggregateInventoryValueAndStockByProduct() {
        StockDetail first = new StockDetail(PRODUCT_ID_1, "Lemons", 10.0, 2.0f, new Date(1000));
        StockDetail second = new StockDetail(PRODUCT_ID_1, "Lemons", 12.0, 3.0f, new Date(2000));
        StockDetail third = new StockDetail(PRODUCT_ID_2, "Milk", 8.0, 1.0f, new Date(1500));

        InventoryReportServiceImpl service = new InventoryReportServiceImpl(
                (from, to) -> Stream.of(first, second, third)
        );

        InventoryReport report = service.getReport(fromDate, toDate);

        assertEquals(64.0, report.getTotalInventoryValue(), 0.001);
        assertEquals(2, report.getStockDetails().size());
        assertEquals(5.0f, report.getStockDetails().get(PRODUCT_ID_1).getTotalNumberInStock(), 0.001f);
        assertEquals(12.0, report.getStockDetails().get(PRODUCT_ID_1).getLatestCost(), 0.001);
    }

    @Test
    public void shouldSerializeInventoryReportToJson() {
        StockDetail first = new StockDetail(PRODUCT_ID_1, "Lemons", 12.0, 5.0f, new Date(2000));
        StockDetail second = new StockDetail(PRODUCT_ID_2, "Milk", 8.0, 1.0f, new Date(1500));

        InventoryReportServiceImpl service = new InventoryReportServiceImpl(
                (from, to) -> Stream.of(first, second)
        );

        InventoryReport report = service.getReport(fromDate, toDate);
        String json = service.toJson(report);

        assertTrue(json.contains("\"reportDate\":\"Dec-1969\""));
        assertTrue(json.contains("\"totalInventoryValue\":68.0"));
        assertTrue(json.contains("\"items\":["));
        assertTrue(json.contains("\"name\":\"Lemons\""));
        assertTrue(json.contains("\"name\":\"Milk\""));
        assertTrue(json.contains("\"status\":\"In Stock\""));
    }

    @Test
    public void shouldThrowWhenInventoryReportIsNullDuringJsonSerialization() {
        InventoryReportServiceImpl service = new InventoryReportServiceImpl(
                (from, to) -> Stream.empty()
        );

        InvalidInventoryReportException exception = org.junit.Assert.assertThrows(
                InvalidInventoryReportException.class,
                () -> service.toJson(null)
        );

        assertEquals("Inventory report must not be null.", exception.getMessage());
    }

    @Test
    public void shouldThrowWhenInventoryDetailsAreNullDuringJsonSerialization() {
        InventoryReportServiceImpl service = new InventoryReportServiceImpl(
                (from, to) -> Stream.empty()
        );
        InventoryReport report = new InventoryReport(fromDate, toDate, 0.0, null);

        InvalidInventoryReportException exception = org.junit.Assert.assertThrows(
                InvalidInventoryReportException.class,
                () -> service.toJson(report)
        );

        assertEquals("Inventory report details must not be null.", exception.getMessage());
    }
}
