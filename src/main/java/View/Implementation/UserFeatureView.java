package View.Implementation;

import Model.LoginUser;
import Model.User;
import Service.LoginService;
import Service.UserService;
import Utils.IO;
import View.UserFeature;

import java.util.Arrays;

public class UserFeatureView implements UserFeature {
    private final LoginService loginService;
    private final UserService userService;

    public UserFeatureView(LoginService loginService, UserService userService) {
        this.loginService = loginService;
        this.userService = userService;
    }

    @Override
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

    @Override
    public void logoutSession() {
        IO.println("Logout...");
        loginService.signOut();
        this.clear();
    }

    @Override
    public void selectFeatureOption(int selectedOption) {
        LoginUser currentLoginUser = loginService.getCurrentUserLogin();
        String userRole = currentLoginUser.getRole();
        if (userRole.equalsIgnoreCase("Admin")) {
            switch (selectedOption) {
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

    private void createNewUser() {
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
            String[] roles = new String[] {"admin", "manager", "cashier"};
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

    private void displayUsers() {
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

    private void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
