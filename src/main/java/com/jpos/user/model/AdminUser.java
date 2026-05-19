package com.jpos.user.model;

import java.util.UUID;

public class AdminUser extends User {
    private final String[] accessFeatures = new String[] {
            "Create New User",
            "Show All Users",
            "Create New Product",
            "Show All Products",
            "Create New Stock Item",
            "Create New Item Price",
            "View Sale Report",
            "View Inventory Report",
            "Check Latest Price for Product",
            "Start Sale"
    };

    public AdminUser(String username, String password) {
        super(username, "Admin");
        this.setPassword(password);
        this.setId(UUID.randomUUID());
    }

    @Override
    public String[] getAccessFeatures() {
        return accessFeatures;
    }
}
