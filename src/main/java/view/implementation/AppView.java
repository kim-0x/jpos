package view.implementation;

import utils.IO;
import utils.WelcomeMessage;
import view.*;
import com.jpos.user.model.UserRole;

public class AppView {
    private final AppMenu appMenu;

    private final UserFeature userFeature;
    private final ProductFeature productFeature;
    private final InventoryFeature inventoryFeature;
    private final SaleFeature saleFeature;

    public AppView(AppMenu appMenu,
                   UserFeature userFeature,
                   ProductFeature productFeature,
                   InventoryFeature inventoryFeature,
                   SaleFeature saleFeature) {

        this.appMenu = appMenu;

        this.userFeature = userFeature;
        this.productFeature = productFeature;
        this.inventoryFeature = inventoryFeature;
        this.saleFeature = saleFeature;
    }

    public void createSession() {
        int option = -1;
        String choice = "";
        try {
            userFeature.loginForm();
            while (true) {
                int selectedOption = option;
                if (option == -1) {
                    selectedOption = appMenu.selectAppMenu();
                }

                if (selectedOption != -1) {
                    this.selectFeatureOption(selectedOption);
                }

                choice = IO.readln("Select an option to start or type 'quit', 'logout' to exit main menu: ");
                if (choice.equals("logout")) {
                    userFeature.logoutSession();
                    WelcomeMessage.displayWelcomeMessage();
                    userFeature.loginForm();
                    option = -1;
                    continue;
                }

                if (choice.equalsIgnoreCase("quit")) {
                    return;
                }

                option = Integer.parseInt(choice);
            }
        } catch(NumberFormatException e) {
            IO.println(String.format("ERROR: Create session with selected option '%s' - Application Exit.", choice));
        }
    }

    private void selectFeatureOption(int selectedOption) {
        UserRole userRole = userFeature.getCurrentUserRole();
        if (userRole == UserRole.ADMIN) {
            switch (selectedOption) {
                case 1:
                    userFeature.createNewUser();
                    break;
                case 2:
                    userFeature.displayUsers();
                    break;
                case 3:
                    productFeature.createNewProduct();
                    break;
                case 4:
                    productFeature.displayProducts();
                    break;
                case 5:
                    inventoryFeature.stockEntry();
                    break;
                case 6:
                    saleFeature.setProductPrice();
                    break;
                case 8:
                    inventoryFeature.displayStockReport();
                    break;
                case 9:
                    saleFeature.getCurrentProductPrice();
                    break;
                case 10:
                    saleFeature.processSaleTransaction();
                    break;
                default:
                    IO.println("Feature is not implemented yet.");
                    break;
            }
        } else if (userRole == UserRole.MANAGER) {
            switch (selectedOption) {
                case 1:
                    productFeature.createNewProduct();
                    break;
                case 2:
                    productFeature.displayProducts();
                    break;
                case 3:
                    inventoryFeature.stockEntry();
                    break;
                case 4:
                    inventoryFeature.displayStockReport();
                    break;
                case 5:
                    saleFeature.getCurrentProductPrice();
                    break;
                default:
                    IO.println("Feature is not implemented yet.");
                    break;
            }
        }
        else {
            switch (selectedOption) {
                case 1:
                    saleFeature.getCurrentProductPrice();
                    break;
                case 2:
                    saleFeature.processSaleTransaction();
                    break;
                default:
                    IO.println("Feature is not implemented yet.");
                    break;
            }
        }
    }
}
