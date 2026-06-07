package com.jpos.report.model;

public enum ReorderStatus {
    IN_STOCK("In Stock"),
    LOW_STOCK("Low Stock"),
    OUT_OF_STOCK("Out of Stock");

    private final String value;

    ReorderStatus(String value) { this.value = value; }

    public String getValue() { return value; }

    @Override
    public String toString() {
        return value;
    }

    public static com.jpos.report.model.ReorderStatus fromString(String reorderStatus) {
        for(ReorderStatus status : ReorderStatus.values()) {
            if (status.value.equalsIgnoreCase(reorderStatus)) {
                return status;
            }
        }

        throw new IllegalArgumentException(String.format("Unknown reorder status: '%s'", reorderStatus));
    }
}
