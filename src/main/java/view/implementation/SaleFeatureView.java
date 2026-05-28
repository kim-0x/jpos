package view.implementation;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.sale.SaleFacade;
import com.jpos.sale.model.SaleItem;
import com.jpos.sale.model.SaleItemData;
import utils.IO;
import view.SaleFeature;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

public class SaleFeatureView implements SaleFeature {
    private final SaleFacade saleFacade;

    public SaleFeatureView(SaleFacade saleFacade) {
        this.saleFacade = saleFacade;
    }

    @Override
    public void setProductPrice() {
        while (true) {
            try {
                var barcode = utils.IO.readln("Enter barcode: ");
                var productQuery = new ProductQuery(null, barcode);
                float margin = enterMargin();

                saleFacade.setProductPrice(productQuery, margin);
                var answer = utils.IO.readln("Product price has been updated. Do you want to continue? (y/n): ");
                if (answer.equalsIgnoreCase("n")) {
                    break;
                }
            }
            catch(NumberFormatException e) {
                var continuing = IO.readln(String.format("%s. Do you want to continue? (y/n): ", e.getMessage()));
                if (continuing.equalsIgnoreCase("n")) {
                    break;
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void getCurrentProductPrice() {
        while (true) {
            try {
                var barcode = utils.IO.readln("Enter barcode: ");
                var productQuery = new ProductQuery(null, barcode);
                var priceBook = saleFacade.getCurrentProductPrice(productQuery);

                if  (priceBook == null) {
                    IO.println("No current product price found!");
                } else {
                    DecimalFormat df = new DecimalFormat("$###,###.00");
                    String priceFormat = df.format(priceBook.getSalePrice());
                    IO.println(String.format("Current product price is: %s", priceFormat));
                }

                var answer = utils.IO.readln("Do you want to continue? (y/n): ");
                if (answer.equalsIgnoreCase("n")) {
                    break;
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void processSaleTransaction() {
        while (true) {
            String receiptNumber = generateReceiptNumber();
            ArrayList<SaleItemData> transactions = new ArrayList<>();
            try {
                while (true) {
                    var barcode = utils.IO.readln("Enter barcode or type 'done' to complete transactions: ");

                    if (barcode.equalsIgnoreCase("done")) {
                        break;
                    }

                    transactions.add(new SaleItemData(barcode, 1));
                }

                var transactionId = saleFacade.processSaleTransaction(receiptNumber,
                        transactions.toArray(new SaleItemData[0]));
                IO.println(String.format("Transaction completed: %s", transactionId));
                displayTransaction(transactionId);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            var answer = utils.IO.readln("Next customer? (y/n): ");
            if (answer.equalsIgnoreCase("n")) {
                break;
            }
        }
    }

    private float enterMargin() {
        while (true) {
            var marginInput = utils.IO.readln("Enter product margin (as a decimal, e.g. 0.2 for 20%): ");
            float margin = Float.parseFloat(marginInput);

            if (margin < 0 || margin > 1) {
                IO.println("Product margin must be between 0 and 1.");
                continue;
            }
            return margin;
        }
    }

    private String generateReceiptNumber() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");
        return String.format("REC-%s", dtf.format(now));
    }

    private void displayTransaction(UUID transactionId) {
        var saleTransaction = saleFacade.getTransactionById(transactionId);
        IO.println(String.format("Receipt Number: %s", saleTransaction.getHeader().getReceiptNumber()));
        System.out.printf("%s%n", "-".repeat(110));
        System.out.printf("%-45s %-20s %-20s %-5s%n", "ProductName", "Quantity", "Price", "Total");
        System.out.printf("%s%n", "-".repeat(110));
        SaleItem[] saleItems = saleTransaction.getSaleItems();
        DecimalFormat df = new DecimalFormat("$###,###.00");
        for (SaleItem saleItem : saleItems) {
            String priceFormatted = df.format(saleItem.getPrice());
            String totalPriceFormatted = df.format(saleItem.getTotalPrice());
            System.out.printf("%-45s %5s %20s %20s%n",
                    saleItem.getProductId(),
                    saleItem.getQuantity(),
                    priceFormatted,
                    totalPriceFormatted);
        }

        System.out.printf("%s%n", "-".repeat(110));
        String grandTotalFormatted = df.format(saleTransaction.getHeader().getGrandTotal());
        IO.println(String.format("Grad Total: %81s%n", grandTotalFormatted));
    }
}
