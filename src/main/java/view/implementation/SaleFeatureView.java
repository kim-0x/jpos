package view.implementation;

import com.jpos.inventory.model.ProductQuery;
import com.jpos.sale.SaleFacade;
import com.jpos.sale.model.SaleItemData;
import com.jpos.sale.model.SaleTransaction;
import utils.IO;
import view.SaleFeature;

import java.text.DecimalFormat;

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
        //String receiptNumber, SaleItemData[] items
    }

    @Override
    public SaleTransaction getSaleTransaction(String receiptNumber) {
        return null;
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
}
