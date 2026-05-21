package view.implementation;

import com.jos.inventory.InventoryFacade;
import utils.IO;
import view.ProductFeature;

public class ProductFeatureView implements ProductFeature {
    private final InventoryFacade inventoryFacade;

    public ProductFeatureView(InventoryFacade inventoryFacade) {
        this.inventoryFacade = inventoryFacade;
    }

    @Override
    public void createNewProduct() {
        try {
            while(true) {
                var barcode = IO.readln("Enter barcode: ");
                var productName = IO.readln("Enter product name: ");
                var category = selectCategory();
                inventoryFacade.createNewProduct(barcode, productName, category);
                var answer = IO.readln("New product has been created. Do you want to continue? (y/n): ");
                if (answer.equalsIgnoreCase("n")) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String selectCategory() {
        StringBuilder builder = new StringBuilder();
        String[] categorySelectionList = inventoryFacade.getCategories();
        while (true) {
            int selectIndex = 1;

            for(String category: categorySelectionList) {
                builder.append(String.format("%d - %s\n", selectIndex, category));
                selectIndex++;
            }
            var userSelectOption = IO.readln(String.format("Select product category \n%s: ",  builder));
            int optionIndex = Integer.parseInt(userSelectOption) - 1;
            if (optionIndex < 0 || optionIndex >= categorySelectionList.length) {
                IO.println("Select a valid category. Please number in the range.");
                continue;
            }

            return categorySelectionList[optionIndex];
        }
    }
}
