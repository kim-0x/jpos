package View;

import Model.LoginUser;
import Model.User;
import Service.LoginService;
import Service.UserService;

import java.util.Arrays;

public class UserView {
    private final LoginService loginService;
    private final UserService userService;

    public UserView(LoginService loginService, UserService userService) {
        this.loginService = loginService;
        this.userService = userService;
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
            if (roleIndex < 0 || roleIndex > 3) {
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
}
