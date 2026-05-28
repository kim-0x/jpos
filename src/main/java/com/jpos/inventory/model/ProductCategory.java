package com.jpos.inventory.model;

public enum ProductCategory {
    FOOD("food"),
    BEVERAGE("beverage"),
    HOUSEHOLD("household"),
    FRUIT("fruit"),
    DAIRY("dairy");

    private final String value;

    ProductCategory(String value) {
        this.value = value;
    }

    /** Returns the storage/display name (e.g. "food", "beverage"). */
    public String getValue() {
        return value;
    }

    /**
     * Returns the {@link ProductCategory} matching the given string, case-insensitively.
     *
     * @throws IllegalArgumentException if no matching category is found.
     */
    public static ProductCategory fromString(String category) {
        for (ProductCategory c : values()) {
            if (c.value.equalsIgnoreCase(category)) {
                return c;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown category: '%s'", category));
    }

    @Override
    public String toString() {
        return value;
    }
}
