package view.implementation;

import utils.IO;
import utils.WelcomeMessage;
import view.AppMenu;
import view.ProductFeature;
import view.UserFeature;

public class AppView {
    private final AppMenu appMenu;

    private final UserFeature userFeature;
    private final ProductFeature productFeature;

    public AppView(AppMenu appMenu, UserFeature userFeature, ProductFeature productFeature) {
        this.appMenu = appMenu;

        this.userFeature = userFeature;
        this.productFeature = productFeature;
    }

    public void createSession() {
        userFeature.loginForm();
        int option = -1;
        while (true) {
            int selectedOption = option;
            if (option == -1) {
                selectedOption = appMenu.selectAppMenu();
            }

            if (selectedOption != -1) {
                this.selectFeatureOption(selectedOption);
            }

            var choice = IO.readln("Select an option to start or type 'quit', 'logout' to exit main menu: ");
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
    }

    private void selectFeatureOption(int selectedOption) {
        String userRole = userFeature.getCurrentUserRole();
        if (userRole.equalsIgnoreCase("Admin")) {
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
                default:
                    IO.println("Feature is not implemented yet.");
                    break;
            }
        } else if (userRole.equalsIgnoreCase("Manager")) {
            switch (selectedOption) {
                case 1:
                    productFeature.createNewProduct();
                    break;
                default:
                    IO.println("Feature is not implemented yet.");
                    break;
            }
        }
        else {
            IO.println("Feature is not implemented yet.");
        }
    }
}
