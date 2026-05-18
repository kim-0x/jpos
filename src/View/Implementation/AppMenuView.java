package View.Implementation;

import Model.*;
import Service.LoginService;
import Utils.UserBuilder;
import View.AppMenu;

public class AppMenuView implements AppMenu {
    private final LoginService loginService;

    public AppMenuView(LoginService loginService) {
        this.loginService = loginService;
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
        var currentLoginUser = loginService.getCurrentUserLogin();
        this.displayCurrentUserGreeting(currentLoginUser.getUsername());
        User user = UserBuilder.createUser(currentLoginUser.getUsername(), "", currentLoginUser.getRole());
        if (user == null) {
            IO.println("Invalid user role. Access denied.");
            return -1;
        }

        String[] accessFeatures = getAccessFeatures(user);
        printAppMenu(accessFeatures);

        while (true) {
            var choice = IO.readln("Select an option to start or type 'quit' to exit: ");

            if (choice.equalsIgnoreCase("quit")) {
                return -1;
            }

            // parseInt will throw exception if input is not a number. Assume user input the correct number for now.
            int option = Integer.parseInt(choice);
            if (option > 0 && option < accessFeatures.length + 1) {
                IO.println("Select a valid option: " + choice);
                return option;
            }
        }
    }

    private void displayCurrentUserGreeting(String username) {
        IO.println("Hello, " + username);
    }

    private String[] getAccessFeatures(User user) {
        if (user instanceof AdminUser) {
            return ((AdminUser)user).getAccessFeatures();
        } else if (user instanceof ManagerUser) {
            return ((ManagerUser)user).getAccessFeatures();
        } else if  (user instanceof CashierUser) {
            return ((CashierUser)user).getAccessFeatures();
        }
        return new String[0];
    }

    private void printAppMenu(String[] accessFeatures) {
        System.out.printf("%s%n", "-".repeat(42));
        System.out.printf("%-10s %12s %18s%n", "|", "APP MENU", "|");
        System.out.printf("%s%n", "-".repeat(42));
        for (int i = 0; i < accessFeatures.length; i++) {
            System.out.printf("%-5s %-30s %5s%n", String.join("", "| " + (i + 1) + "."), accessFeatures[i], "|" );
        }
        System.out.printf("%s%n", "-".repeat(42));
    }
}
