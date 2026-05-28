package com.jpos.user.model;

import java.util.UUID;

public class ManagerUser extends User {
    private final String[] accessFeatures = new String[] {
            "Create New Product",
            "Show All Products",
            "Create New Stock Item",
            "View Inventory Report",
            "Check Latest Price for Product"
    };

    public ManagerUser(String userName, String password) {
        super(userName, UserRole.MANAGER.getValue());
        this.setPassword(password);
        this.setId(UUID.randomUUID());
    }

    @Override
    public String[] getAccessFeatures() {
        return accessFeatures;
    }
}
