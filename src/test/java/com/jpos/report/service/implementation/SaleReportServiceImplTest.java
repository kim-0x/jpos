package com.jpos.report.service.implementation;

import com.jpos.inventory.model.StockRecord;
import com.jpos.report.model.SaleDetail;
import com.jpos.report.model.SaleReport;
import com.jpos.report.model.SaleSummary;
import com.jpos.report.service.SaleGateway;
import com.jpos.sale.model.*;
import com.jpos.sale.service.InventoryGateway;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class SaleReportServiceImplTest {

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
    public void shouldReturnZeroSummaryWhenNoTransactions() {
        SaleReportServiceImpl service = new SaleReportServiceImpl(
                (from, to) -> Stream.empty(),
                stubInventoryGateway()
        );

        SaleReport report = service.getReport(fromDate, toDate);

        SaleSummary summary = report.getSaleSummary();
        assertEquals(0.0, summary.getTotalRevenue(), 0.001);
        assertEquals(0.0, summary.getTotalCost(), 0.001);
        assertEquals(0.0, summary.getTotalProfit(), 0.001);
    }

    @Test
    public void shouldReturnEmptyDetailsWhenNoTransactions() {
        SaleReportServiceImpl service = new SaleReportServiceImpl(
                (from, to) -> Stream.empty(),
                stubInventoryGateway()
        );

        SaleReport report = service.getReport(fromDate, toDate);

        assertTrue(report.getSaleDetails().isEmpty());
    }

    @Test
    public void shouldSetDateRangeInReport() {
        SaleReportServiceImpl service = new SaleReportServiceImpl(
                (from, to) -> Stream.empty(),
                stubInventoryGateway()
        );

        SaleReport report = service.getReport(fromDate, toDate);

        assertEquals(fromDate, report.getFromDate());
        assertEquals(toDate, report.getToDate());
    }

    @Test
    public void shouldCalculateCorrectSummaryForSingleTransactionSingleItem() {
        // qty=1, cost=30, price=50 → grandTotal=50, cost=30, profit=20
        SaleTransaction transaction = buildTransaction("REC-001", new SaleItemSpec(PRODUCT_ID_1, 1f, 30.0, 50.0));

        SaleReportServiceImpl service = new SaleReportServiceImpl(
                stubSaleGateway(transaction),
                stubInventoryGateway()
        );

        SaleReport report = service.getReport(fromDate, toDate);
        SaleSummary summary = report.getSaleSummary();

        assertEquals(50.0, summary.getTotalRevenue(), 0.001);
        assertEquals(30.0, summary.getTotalCost(), 0.001);
        assertEquals(20.0, summary.getTotalProfit(), 0.001);
    }

    @Test
    public void shouldPopulateSaleDetailForSingleItem() {
        // qty=1, cost=20, price=40 → SaleDetail: qty=1, totalCost=20, totalPrice=40
        SaleTransaction transaction = buildTransaction("REC-001", new SaleItemSpec(PRODUCT_ID_1, 1f, 20.0, 40.0));

        SaleReportServiceImpl service = new SaleReportServiceImpl(
                stubSaleGateway(transaction),
                stubInventoryGateway()
        );

        SaleReport report = service.getReport(fromDate, toDate);
        SaleDetail detail = report.getSaleDetails().get(PRODUCT_ID_1);

        assertNotNull(detail);
        assertEquals("Product 1", detail.getProductName());
        assertEquals(1f, detail.getTotalQuantity(), 0.001f);
        assertEquals(20.0, detail.getTotalCost(), 0.001);
        assertEquals(40.0, detail.getTotalRevenue(), 0.001);
    }

    @Test
    public void shouldAggregateSummaryAcrossMultipleTransactions() {
        // txn1: qty=1, cost=30, price=50 → revenue=50, cost=30, profit=20
        // txn2: qty=1, cost=10, price=25 → revenue=25, cost=10, profit=15
        // combined: revenue=75, cost=40, profit=35
        SaleTransaction txn1 = buildTransaction("REC-001", new SaleItemSpec(PRODUCT_ID_1, 1f, 30.0, 50.0));
        SaleTransaction txn2 = buildTransaction("REC-002", new SaleItemSpec(PRODUCT_ID_2, 1f, 10.0, 25.0));

        SaleReportServiceImpl service = new SaleReportServiceImpl(
                stubSaleGateway(txn1, txn2),
                stubInventoryGateway()
        );

        SaleReport report = service.getReport(fromDate, toDate);
        SaleSummary summary = report.getSaleSummary();

        assertEquals(75.0, summary.getTotalRevenue(), 0.001);
        assertEquals(40.0, summary.getTotalCost(), 0.001);
        assertEquals(35.0, summary.getTotalProfit(), 0.001);
    }

    @Test
    public void shouldCreateSeparateDetailsForDifferentProductsInSingleTransaction() {
        SaleTransaction transaction = buildTransaction("REC-001",
                new SaleItemSpec(PRODUCT_ID_1, 1f, 10.0, 20.0),
                new SaleItemSpec(PRODUCT_ID_2, 1f, 15.0, 30.0)
        );

        SaleReportServiceImpl service = new SaleReportServiceImpl(
                stubSaleGateway(transaction),
                stubInventoryGateway()
        );

        SaleReport report = service.getReport(fromDate, toDate);
        Map<UUID, SaleDetail> details = report.getSaleDetails();

        assertEquals(2, details.size());
        assertNotNull(details.get(PRODUCT_ID_1));
        assertNotNull(details.get(PRODUCT_ID_2));
    }

    @Test
    public void shouldAccumulateSaleDetailsForSameProductAcrossTransactions() {
        // txn1: PRODUCT_ID_1, qty=1, cost=20, price=40
        // txn2: PRODUCT_ID_1, qty=1, cost=20, price=40
        // accumulated: qty=2, totalCost=40, totalPrice=80
        SaleTransaction txn1 = buildTransaction("REC-001", new SaleItemSpec(PRODUCT_ID_1, 1f, 20.0, 40.0));
        SaleTransaction txn2 = buildTransaction("REC-002", new SaleItemSpec(PRODUCT_ID_1, 1f, 20.0, 40.0));

        SaleReportServiceImpl service = new SaleReportServiceImpl(
                stubSaleGateway(txn1, txn2),
                stubInventoryGateway()
        );

        SaleReport report = service.getReport(fromDate, toDate);
        SaleDetail detail = report.getSaleDetails().get(PRODUCT_ID_1);

        assertNotNull(detail);
        assertEquals(2f, detail.getTotalQuantity(), 0.001f);
        assertEquals(40.0, detail.getTotalCost(), 0.001);
        assertEquals(80.0, detail.getTotalRevenue(), 0.001);
    }

    @Test
    public void shouldReturnOnlyOneDetailEntryPerUniqueProduct() {
        SaleTransaction txn1 = buildTransaction("REC-001", new SaleItemSpec(PRODUCT_ID_1, 1f, 20.0, 40.0));
        SaleTransaction txn2 = buildTransaction("REC-002", new SaleItemSpec(PRODUCT_ID_1, 1f, 20.0, 40.0));
        SaleTransaction txn3 = buildTransaction("REC-003", new SaleItemSpec(PRODUCT_ID_2, 1f, 15.0, 30.0));

        SaleReportServiceImpl service = new SaleReportServiceImpl(
                stubSaleGateway(txn1, txn2, txn3),
                stubInventoryGateway()
        );

        SaleReport report = service.getReport(fromDate, toDate);

        assertEquals(2, report.getSaleDetails().size());
    }

    // --- helpers ---

    private record SaleItemSpec(UUID productId, float qty, double cost, double price) {}

    private SaleTransaction buildTransaction(String receiptNumber, SaleItemSpec... specs) {
        UUID txnId = UUID.randomUUID();
        SaleHeader header = new SaleHeader(txnId, receiptNumber, 0, new Date());
        SaleTransaction transaction = new SaleTransaction(header);
        for (SaleItemSpec spec : specs) {
            transaction.addItem(new SaleItem(spec.productId(), spec.qty(), spec.cost(), spec.price(), txnId));
        }
        return transaction;
    }

    private SaleGateway stubSaleGateway(SaleTransaction... transactions) {
        return (from, to) -> Stream.of(transactions);
    }

    private InventoryGateway stubInventoryGateway() {
        return new InventoryGateway() {
            @Override
            public ProductInfo findBy(ProductRef ref) {
                if (PRODUCT_ID_1.equals(ref.productId())) {
                    return new ProductInfo(PRODUCT_ID_1, "BAR-001", "Product 1", 10.0);
                }
                return new ProductInfo(PRODUCT_ID_2, "BAR-002", "Product 2", 15.0);
            }

            @Override
            public void reduceStock(ProductRef ref, float numberOfStock) {}

            @Override
            public Stream<StockRecord> getAllStockOutTransaction(Date fromDate, Date toDate) {
                return Stream.empty();
            }
        };
    }
}
