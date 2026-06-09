package com.jpos.user.model;

import java.io.Serial;
import java.util.UUID;

public class AdminUser extends User {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String[] accessFeatures = new String[] {
            "Create New User",
            "Show All Users",
            "Create New Product",
            "Show All Products",
            "Create New Stock Item",
            "Create New Item Price",
            "View Sale Report",
            "View Inventory Report",
            "Check Latest Inventory",
            "Check Latest Price for Product",
            "Start Sale",
            "Export Reports to JSON"
    };

    public AdminUser(String username, String password) {
        super(username, UserRole.ADMIN.getValue());
        this.setPassword(password);
        this.setId(UUID.randomUUID());
    }

    @Override
    public String[] getAccessFeatures() {
        return accessFeatures;
    }
}
