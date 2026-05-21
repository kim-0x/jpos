package view.implementation;

import com.jpos.user.UserFacade;
import com.jpos.user.exception.AdminAlreadyExistsException;
import com.jpos.user.exception.UnauthorizedUserActionException;
import com.jpos.user.exception.UsernameAlreadyExistsException;
import com.jpos.user.model.LoginUser;
import com.jpos.user.model.User;
import utils.IO;
import view.UserFeature;

import java.util.Arrays;

public class UserFeatureView implements UserFeature {
    private final UserFacade userFacade;

    public UserFeatureView(UserFacade userFacade) {
        this.userFacade = userFacade;
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
            boolean isSucceed = userFacade.signIn(username, String.valueOf(password));

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
        userFacade.signOut();
        this.clear();
    }

    @Override
    public void createNewUser() {
        try {
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

                userFacade.createNewUser(username, String.valueOf(password), roles[roleIndex]);

                IO.println("User created successfully.");
                break;
            }
        } catch (UsernameAlreadyExistsException
                 | AdminAlreadyExistsException
                 | InstantiationException
                 | UnauthorizedUserActionException e) {
            IO.println(e.getMessage());
        }
    }

    @Override
    public void displayUsers() {
        IO.println("User Accounts:");
        try {
            LoginUser currentLoginUser = userFacade.getCurrentUserLogin();
            User[] allUsers = userFacade.getUsers(currentLoginUser.getRole());
            System.out.printf("%s%n", "-".repeat(70));
            System.out.printf("%-35s %20s %10s%n", "ID", "Name", "Role");
            System.out.printf("%s%n", "-".repeat(70));
            for (User user : allUsers) {
                System.out.printf("%-35s %20s %10s%n", user.getId(), user.getUsername(), user.getRole());
            }
        } catch (UnauthorizedUserActionException e) {
           IO.println(e.getMessage());
        }
    }

    @Override
    public String getCurrentUserRole() {
        LoginUser currentLoginUser = userFacade.getCurrentUserLogin();
        return currentLoginUser.getRole();
    }

    private void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
