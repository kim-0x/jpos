package com.jpos.user.utils;

import com.jpos.user.model.AdminUser;
import com.jpos.user.model.CashierUser;
import com.jpos.user.model.ManagerUser;
import com.jpos.user.model.User;
import com.jpos.user.model.UserRole;

public class UserBuilder {
    public static User createUser(String username, String password, String role) {
        try {
            return createUser(username, password, UserRole.fromString(role));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static User createUser(String username, String password, UserRole role) {
        return switch (role) {
            case ADMIN -> new AdminUser(username, password);
            case MANAGER -> new ManagerUser(username, password);
            case CASHIER -> new CashierUser(username, password);
        };
    }
}
