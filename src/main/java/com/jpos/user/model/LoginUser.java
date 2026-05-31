package com.jpos.user.model;

import java.io.Serial;

public class LoginUser extends BaseUser {
    @Serial
    private static final long serialVersionUID = 1L;

    public LoginUser(String username, String role) {
        super(username, role);
    }
}
