package com.jpos.user.utils;

import com.jpos.user.model.AdminUser;
import com.jpos.user.model.CashierUser;
import com.jpos.user.model.ManagerUser;
import com.jpos.user.model.User;

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
