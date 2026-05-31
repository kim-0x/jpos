package com.jpos.user.model;

import java.io.Serial;
import java.util.UUID;

public abstract class User extends BaseUser {
    @Serial
    private static final long serialVersionUID = 1L;

    private String password;
    private UUID id;

    public User(String username, String role) {
        super(username, role);
    }

    public abstract String[] getAccessFeatures();

    public String getPassword() {
        return password;
    }

    protected void setPassword(String password) {
        this.password = password;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
