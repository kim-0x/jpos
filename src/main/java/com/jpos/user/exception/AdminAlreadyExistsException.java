package com.jpos.user.exception;

public class AdminAlreadyExistsException extends RuntimeException {
    public AdminAlreadyExistsException() {
        super("Multiple Admin users do not supported yet.");
    }
}
