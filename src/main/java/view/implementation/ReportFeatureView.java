package view.implementation;

import com.jpos.report.ReportFacade;
import com.jpos.report.model.InventoryReport;
import com.jpos.report.model.SaleReport;
import utils.IO;
import view.ReportFeature;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class ReportFeatureView implements ReportFeature {
    private final ReportFacade reportFacade;

    public ReportFeatureView(ReportFacade reportFacade) {
        this.reportFacade = reportFacade;
    }

    @Override
    public void getSaleReport() {
        while (true) {
            try {
                var fromDateInput = utils.IO.readln("Enter from date (YYYY-MM-DD): ");
                var toDateInput = utils.IO.readln("Enter to date (YYYY-MM-DD): ");

                LocalDate fromDate = LocalDate.parse(fromDateInput);
                LocalDate toDate = LocalDate.parse(toDateInput);

                Date reportStart = Date.from(fromDate.atTime(LocalTime.MIN)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
                Date reportEnd = Date.from(toDate.atTime(LocalTime.MAX)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());

                SaleReport report = reportFacade.getSaleReport(reportStart, reportEnd);

                if (report == null || report.getSaleDetails().isEmpty()) {
                    System.out.println("No report has been displayed");
                    return;
                }

                this.displaySaleReport(report);

                var continuing = IO.readln("Do you want to continue? (y/n): ");
                if (continuing.equalsIgnoreCase("n")) {
                    break;
                }

            } catch (DateTimeParseException e) {
                var continuing = IO.readln(String.format("%s. Unable to process report with given date range." +
                        " Do you want to continue? (y/n): ", e.getMessage()));
                if (continuing.equalsIgnoreCase("n")) {
                    break;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void getInventoryReport() {
        while (true) {
            try {
                var fromDateInput = utils.IO.readln("Enter from date (YYYY-MM-DD): ");
                var toDateInput = utils.IO.readln("Enter to date (YYYY-MM-DD): ");

                LocalDate fromDate = LocalDate.parse(fromDateInput);
                LocalDate toDate = LocalDate.parse(toDateInput);

                Date reportStart = Date.from(fromDate.atTime(LocalTime.MIN)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
                Date reportEnd = Date.from(toDate.atTime(LocalTime.MAX)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());

                InventoryReport report = reportFacade.getInventoryReport(reportStart, reportEnd);

                if (report == null || report.getStockDetails().isEmpty()) {
                    System.out.println("No report has been displayed");
                    return;
                }

                this.displayInventoryReport(report);

                var continuing = IO.readln("Do you want to continue? (y/n): ");
                if (continuing.equalsIgnoreCase("n")) {
                    break;
                }

            } catch (DateTimeParseException e) {
                var continuing = IO.readln(String.format("%s. Unable to process report with given date range." +
                        " Do you want to continue? (y/n): ", e.getMessage()));
                if (continuing.equalsIgnoreCase("n")) {
                    break;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void displayInventoryReport(InventoryReport inventoryReport) {
        System.out.printf("%n%nInventory Report%n");
        System.out.printf("From: %s%n", inventoryReport.getFromDate());
        System.out.printf("To: %s%n", inventoryReport.getToDate());
        System.out.printf("%s%n", "-".repeat(120));
        System.out.printf("%-30s %20s %20s %20s %20s%n", "Name", "Total Qty", "Reorder Status", "Latest Cost", "Stock Value");
        System.out.printf("%s%n", "-".repeat(120));

        inventoryReport.getStockDetails().forEach((productId, stockDetail) -> {
            double cost = stockDetail.getLatestCost();
            double qty = stockDetail.getTotalNumberInStock();
            double stockValue = stockDetail.getTotalStockValue();

            String productName = stockDetail.getProductName();
            String formatProductName = (productName.length() > 25)
                    ? String.format("%s...", productName.substring(0, 25))
                    : productName;

            System.out.printf("%-30s %20s %20s %20s %20s%n",
                    formatProductName,
                    qty,
                    stockDetail.getReorderStatus().getValue(),
                    accountingFormat(cost),
                    accountingFormat(stockValue)
            );
        });
        System.out.printf("%s%n", "-".repeat(120));
        var totalInventoryValue = inventoryReport.getTotalInventoryValue();
        System.out.printf("%-53s %60s%n", "Total Inventory Value:", accountingFormat(totalInventoryValue));
    }

    private void displaySaleReport(SaleReport saleReport) {
        System.out.printf("%n%nSale Report%n");
        System.out.printf("From: %s%n", saleReport.getFromDate());
        System.out.printf("To: %s%n", saleReport.getToDate());
        System.out.printf("%s%n", "-".repeat(120));
        System.out.printf("%-30s %20s %20s %20s %20s%n", "Name", "Total Qty", "Total Revenue", "Total Cost", "Total Profit");
        System.out.printf("%s%n", "-".repeat(120));

        saleReport.getSaleDetails().forEach((productId, saleDetail) -> {
            double cost = saleDetail.getTotalCost();
            double revenue = saleDetail.getTotalRevenue();
            double profit = revenue - cost;

            String productName = saleDetail.getProductName();
            String formatProductName = (productName.length() > 25)
                    ? String.format("%s...", productName.substring(0, 25))
                    : productName;

            System.out.printf("%-30s %20s %20s %20s %20s%n",
                    formatProductName,
                    saleDetail.getTotalQuantity(),
                    accountingFormat(revenue),
                    accountingFormat(-1 * Math.abs(cost)),
                    accountingFormat(profit)
            );
        });
        System.out.printf("%s%n", "-".repeat(120));
        var summary = saleReport.getSaleSummary();
        System.out.printf("%-51s %20s %20s %20s%n", "Summary:",
                accountingFormat(summary.getTotalRevenue()),
                accountingFormat(-1 * Math.abs(summary.getTotalCost())),
                accountingFormat(summary.getTotalProfit()));
    }

    private String accountingFormat(double money) {
        BigDecimal amount = new BigDecimal(money);
        // The pattern handles: Positive ; Negative
        DecimalFormat accountingFormat = new DecimalFormat("$#,##0.00;($#,##0.00)");
        return accountingFormat.format(amount);
    }
}
