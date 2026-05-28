package com.jpos.inventory.exception;

public class InvalidCategoryException extends RuntimeException {
    public InvalidCategoryException(String category) {
        super(String.format("Invalid category: '%s'", category));
    }
}
