package com.jpos.user.model;

import java.io.Serial;
import java.io.Serializable;

public class BaseUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String username;
    private String role;

    public BaseUser(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    protected void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    protected void setRole(String role) {
        this.role = role;
    }
}
