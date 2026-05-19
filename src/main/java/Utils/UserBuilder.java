package Utils;

import Model.AdminUser;
import Model.CashierUser;
import Model.ManagerUser;
import Model.User;

public class UserBuilder {
    public static User createUser(String username, String password, String role) {
        switch (role.toLowerCase()) {
            case "manager" -> {
                return new ManagerUser(username, password);
            }
            case "cashier" -> {
                return new CashierUser(username, password);
            }
            case "admin" -> {
                return new AdminUser(username, password);
            }
            default -> {
                return null;
            }
        }
    }
}
