package com.jpos.user.repository.implementation.file;

import com.jpos.user.exception.AdminAlreadyExistsException;
import com.jpos.user.exception.UserCreationException;
import com.jpos.user.model.AdminUser;
import com.jpos.user.model.LoginUser;
import com.jpos.user.model.User;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.utils.UserBuilder;
import utils.AbstractBinRepository;

import java.nio.file.Path;
import java.util.ArrayList;

public class BinUserRepository extends AbstractBinRepository<User> implements UserRepository {
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";

    private final ArrayList<User> users = new ArrayList<>();

    public BinUserRepository() {
        this(getDefaultDatFilePath("user.dat"));
    }

    public BinUserRepository(Path filePath) {
        super(filePath);
        loadUsers();
    }

    @Override
    public boolean addUser(String username, String password, String role) throws UserCreationException {
        User user = UserBuilder.createUser(username, password, role);
        if (user == null) {
            throw new UserCreationException("Unable to create user object");
        }

        if (user instanceof AdminUser) {
            throw new AdminAlreadyExistsException();
        }

        users.add(user);
        persistUsers();
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
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
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

    @Override
    protected Class<User> getEntityType() {
        return User.class;
    }

    private void loadUsers() {
        users.clear();
        users.addAll(loadFromDat());

        if (users.isEmpty()) {
            users.add(new AdminUser(DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD));
            persistUsers();
        }
    }

    private void persistUsers() {
        persistToDat(users);
    }
}
