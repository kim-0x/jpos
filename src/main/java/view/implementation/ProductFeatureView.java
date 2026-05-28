package view.implementation;

import com.jpos.inventory.InventoryFacade;
import com.jpos.inventory.model.Product;
import com.jpos.inventory.model.ProductCategory;
import utils.IO;
import view.ProductFeature;

public class ProductFeatureView implements ProductFeature {
    private final InventoryFacade inventoryFacade;

    public ProductFeatureView(InventoryFacade inventoryFacade) {
        this.inventoryFacade = inventoryFacade;
    }

    @Override
    public void createNewProduct() {
        while (true) {
            try {
                var barcode = IO.readln("Enter barcode: ");
                var productName = IO.readln("Enter product name: ");
                ProductCategory category = selectCategory();
                inventoryFacade.createNewProduct(barcode, productName, category);
                var answer = IO.readln("New product has been created. Do you want to continue? (y/n): ");
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
    public void displayProducts() {
        IO.println("Product Catalog:");
        try {
            System.out.printf("%s%n", "*".repeat(110));
            System.out.printf("%-45s %-20s %-30s %-20s%n", "ID", "Barcode", "Name", "Category");
            System.out.printf("%s%n", "*".repeat(110));
            Product[] products = inventoryFacade.getProducts();
            if (products == null || products.length == 0) {
                System.out.println("No product has been displayed.");
                return;
            }

            for (Product product:  products) {
                String productName = product.getName();
                String formatProductName = (productName.length() > 25)
                        ? String.format("%s...",productName.substring(0, 25))
                        : productName;
                System.out.printf("%-45s %-20s %-30s %-20s%n", product.getId(), product.getBarcode(), formatProductName, product.getCategory());
            }
        }catch(Exception e) {
            IO.println(e.getMessage());
        }
    }

    private ProductCategory selectCategory() {
        ProductCategory[] categories = ProductCategory.values();
        while (true) {
            int selectIndex = 1;
            StringBuilder builder = new StringBuilder();
            for (ProductCategory category : categories) {
                builder.append(String.format("%d - %s\n", selectIndex, category.getValue()));
                selectIndex++;
            }
            var userSelectOption = IO.readln(String.format("Select product category \n%s: ", builder));
            try {
                int optionIndex = Integer.parseInt(userSelectOption) - 1;
                if (optionIndex < 0 || optionIndex >= categories.length) {
                    IO.println("Select a valid category. Please enter number in the range.");
                    continue;
                }
                return categories[optionIndex];
            } catch (NumberFormatException e) {
                throw new NumberFormatException(String.format("%s is invalid category selection", userSelectOption));
            }
        }
    }
}