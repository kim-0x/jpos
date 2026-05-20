package com.jpos.user.repository.implementation;

import com.jpos.user.model.AdminUser;
import com.jpos.user.model.LoginUser;
import com.jpos.user.model.User;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.utils.UserBuilder;

import java.util.ArrayList;
import java.util.List;

public class MockUserRepository implements UserRepository {
    private static final ArrayList<User> users = new ArrayList<>(List.of(new AdminUser("admin", "admin")));

    @Override
    public boolean addUser(String username, String password, String role) throws UnsupportedOperationException, InstantiationException {
        User user = UserBuilder.createUser(username, password, role);
        if (user == null) {
            throw new InstantiationException("Unable to create user object");
        }

        if (user instanceof AdminUser) {
           throw new UnsupportedOperationException("Multiple Admin users do not supported yet.");
        }

        users.add(user);
        return true;
    }

    @Override
    public boolean isNameTaken(String name) {
        for (User user : users) {
            if (user.getUsername().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean validUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username)
                    && user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public LoginUser getUserLogin(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return new LoginUser(user.getUsername(), user.getRole());
            }
        }
        return null;
    }

    @Override
    public User[] getUsers() {
        return users.toArray(new User[0]);
    }
}
