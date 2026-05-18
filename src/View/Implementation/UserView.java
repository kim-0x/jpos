package View.Implementation;

import Model.LoginUser;
import Model.User;
import Service.LoginService;
import Service.UserService;
import Utils.WelcomeMessage;
import View.AppMenu;

import java.util.Arrays;

public class UserView {
    private final LoginService loginService;
    private final UserService userService;
    private final AppMenu appMenu;

    public UserView(LoginService loginService, UserService userService, AppMenu appMenu) {
        this.loginService = loginService;
        this.userService = userService;
        this.appMenu = appMenu;
    }

    public void loginForm() {
        while (true) {
            var username = IO.readln("Enter username:");
            var console = System.console();
            if (console == null) {
                IO.println("Console not available. Please run this program from a terminal.");
                return;
            }
            char[] password = console.readPassword("Enter password:");
            boolean isSucceed = loginService.signIn(username, String.valueOf(password));

            if (!isSucceed) {
                IO.println("Invalid username or password. Please try again.");
                continue;
            }
            break;
        }
    }

    /**
     * INTENT: Interactively collect the data required to register a new user account.
     * PRECONDITION: the program is running in a terminal with an available console, and the operator can
     * provide a username, matching password entries, and a valid role selection.
     * RETURNS: nothing.
     * POSTCONDITION: a new user is created when all entered data is valid; otherwise the method keeps
     * prompting until creation succeeds or exits early when no console is available.
     */
    public void createNewUser() {
        while (true) {
            var username = IO.readln("Enter username:");
            var console = System.console();
            if (console == null) {
                IO.println("Console not available. Please run this program from a terminal.");
                return;
            }
            char[] password = console.readPassword("Enter password:");
            char[] confirmPassword = console.readPassword("Confirm password:");
            if (!Arrays.equals(password, confirmPassword)) {
                IO.println("Invalid password. Please try again.");
                continue;
            }
            String[] roles = new String[] {"admin", "manager", "cashier" };
            var inputRole = IO.readln("Enter role (1: Admin, 2: Store Manager, 3: Cashier): ");
            int roleIndex = Integer.parseInt(inputRole) - 1;
            if (roleIndex < 0 || roleIndex >= 3) {
                IO.println("Invalid user role. Please try again.");
                continue;
            }

            boolean isSucceed = userService.addUser(username, String.valueOf(password), roles[roleIndex]);
            if (!isSucceed) {
                IO.println("Invalid username or password. Please try again.");
                continue;
            }

            IO.println("User created successfully.");
            break;
        }
    }

    public void displayUsers() {
        IO.println("User Accounts:");
        LoginUser currentLoginUser = loginService.getCurrentUserLogin();
        User[] allUsers = userService.getUsers(currentLoginUser.getRole());
        System.out.printf("%s%n", "-".repeat(70));
        System.out.printf("%-35s %20s %10s%n", "ID", "Name", "Role");
        System.out.printf("%s%n", "-".repeat(70));
        for (User user : allUsers) {
            System.out.printf("%-35s %20s %10s%n", user.getId(), user.getUsername(), user.getRole());
        }
    }

    public void createSession() {
        this.loginForm();
        int option = -1;
        while (true) {
            int selectedOption = option;
            if (option == -1) {
                selectedOption = appMenu.selectAppMenu();
            }

            if (selectedOption != -1) {
                this.dispatchSelectedOption(selectedOption);
            }

            var choice = IO.readln("Select an option to start or type 'quit', 'logout' to exit main menu: ");
            if (choice.equals("logout")) {
                IO.println("Logout...");
                loginService.signOut();
                this.clear();
                WelcomeMessage.displayWelcomeMessage();
                this.loginForm();
                option = -1;
                continue;
            }

            if (choice.equalsIgnoreCase("quit")) {
                return;
            }

            option = Integer.parseInt(choice);
        }
    }

    private void dispatchSelectedOption(int selectedOption) {
        LoginUser currentLoginUser = loginService.getCurrentUserLogin();
        String userRole = currentLoginUser.getRole();
        if (userRole.equalsIgnoreCase("Admin")) {
            switch(selectedOption) {
                case 1:
                    this.createNewUser();
                    break;

                case 2:
                    this.displayUsers();
                    break;

                default:
                   IO.println("Feature is not implemented yet.");
                   break;
            }

        } else {
            IO.println("Feature is not implemented yet.");
        }
    }

    private void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
