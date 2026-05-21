package com.jpos.user.exception;

public class UnauthorizedUserActionException extends RuntimeException {
    public UnauthorizedUserActionException(String message) {
        super(message);
    }
}
