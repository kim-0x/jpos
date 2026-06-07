package com.jpos.report.model;

public enum ReorderStatus {
    IN_STOCK("InStock"),
    LOW_STOCK("LowStock"),
    OUT_OF_STOCK("OutOfStock");

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
