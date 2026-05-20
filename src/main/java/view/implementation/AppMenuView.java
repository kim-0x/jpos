package view.implementation;

import com.jpos.user.UserFacade;
import utils.IO;
import view.AppMenu;

public class AppMenuView implements AppMenu {
    private final UserFacade userFacade;

    public AppMenuView(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    /**
     * INTENT: Display the application menu available to the logged-in user and collect the next action.
     * PRECONDITION: a user is logged in, the login service can provide that user, and the user's role can
     * be resolved into accessible features.
     * RETURNS: the selected menu option number, or -1 when the user quits or access cannot be resolved.
     * POSTCONDITION: the menu is shown and a valid selection is returned once entered; otherwise the
     * method keeps prompting until the user quits.
     */
    @Override
    public int selectAppMenu() {
        try {
            var currentLoginUser = userFacade.getCurrentUserLogin();
            this.displayCurrentUserGreeting(currentLoginUser.getUsername());
            String[] accessFeatures = userFacade.getAccessFeatures();
            printAppMenu(accessFeatures);

            while (true) {
                var choice = IO.readln("Select an option to start or type 'quit' to exit: ");

                if (choice.equalsIgnoreCase("quit")) {
                    return -1;
                }

                int option = Integer.parseInt(choice);
                boolean isValidKey = userFacade.validAccessFeatureKey(option);
                if (isValidKey) {
                    return option;
                }
            }
        } catch (IndexOutOfBoundsException | InstantiationException e) {
            IO.println(e.getMessage());
            return -1;
        }
    }

    private void displayCurrentUserGreeting(String username) {
        IO.println("Hello, " + username);
    }

    private void printAppMenu(String[] accessFeatures) {
        System.out.printf("%s%n", "-".repeat(42));
        System.out.printf("%-10s %12s %18s%n", "|", "APP MENU", "|");
        System.out.printf("%s%n", "-".repeat(42));
        for (int i = 0; i < accessFeatures.length; i++) {
            System.out.printf("%-5s %-30s %5s%n", String.join("", "| " + (i + 1) + "."), accessFeatures[i], "|");
        }
        System.out.printf("%s%n", "-".repeat(42));
    }
}