package view.implementation;

import com.jos.inventory.InventoryFacade;
import com.jos.inventory.model.Product;
import com.jos.inventory.model.StockRecord;
import view.InventoryFeature;
import utils.IO;

import java.text.DecimalFormat;

public class InventoryFeatureView implements InventoryFeature {
    private final InventoryFacade inventoryFacade;

    public InventoryFeatureView(InventoryFacade inventoryFacade) {
        this.inventoryFacade = inventoryFacade;
    }

    @Override
    public void stockEntry() {
        while(true) {
            try {
                var barcode = IO.readln("Enter barcode: ");
                var inputStockInQuantity = IO.readln("Enter stock in quantity: ");
                float stockInQuantity = Float.parseFloat(inputStockInQuantity);
                var inputCost = IO.readln("Enter cost: ");
                double cost = Double.parseDouble(inputCost);
                inventoryFacade.stockEntry(barcode, cost,  stockInQuantity);
                var answer = IO.readln("New stock entry has been created. Do you want to continue? (y/n): ");
                if (answer.equalsIgnoreCase("n")) {
                    break;
                }
            } catch(Exception e) {
                IO.println(e.getMessage());
            }
        }
    }

    @Override
    public void displayStockReport() {
        IO.println("Inventory Report:");
        try {
            System.out.printf("%s%n", "*".repeat(120));
            System.out.printf("%-20s %-30s %-20s %20s %20s%n", "Barcode", "Name", "Category", "On Hand", "Cost");
            System.out.printf("%s%n", "*".repeat(120));
            StockRecord[] records = inventoryFacade.getStockReport();
            if (records == null || records.length == 0) {
                System.out.println("No report has been displayed");
                return;
            }

            for (StockRecord record : records) {
                Product product = record.getProduct();
                double cost = record.getCost();
                DecimalFormat df = new DecimalFormat("$###,###.00");
                String costFormatted = df.format(cost);

                System.out.printf("%-20s %-30s %-20s %20s %20s%n", product.getBarcode(),
                        product.getName(),
                        product.getCategory(),
                        record.getNumberInStock(),
                        costFormatted);
            }
        } catch(Exception e) {
            IO.println(e.getMessage());
        }
    }
}
