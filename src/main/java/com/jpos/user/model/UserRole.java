package com.jpos.user.model;

public enum UserRole {
    ADMIN("Admin"),
    MANAGER("Manager"),
    CASHIER("Cashier");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    /** Returns the display/storage name (e.g. "Admin", "Manager", "Cashier"). */
    public String getValue() {
        return value;
    }

    /**
     * Returns the {@link UserRole} matching the given string, case-insensitively.
     *
     * @throws IllegalArgumentException if no matching role is found.
     */
    public static UserRole fromString(String role) {
        for (UserRole r : values()) {
            if (r.value.equalsIgnoreCase(role)) {
                return r;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown role: '%s'", role));
    }

    @Override
    public String toString() {
        return value;
    }
}
