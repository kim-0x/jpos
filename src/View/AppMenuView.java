package View;

import Model.*;
import Service.LoginService;
import Utils.UserBuilder;

public class AppMenuView {
    private final LoginService loginService;

    public AppMenuView(LoginService loginService) {
        this.loginService = loginService;
        this.displayWelcomeMessage();
    }

    public int selectAppMenu() {
        var currentLoginUser = loginService.getCurrentUserLogin();
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

    private void displayWelcomeMessage() {
        var currentLoginUser = loginService.getCurrentUserLogin();
        IO.println("Hello, " + currentLoginUser.getUsername());
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
