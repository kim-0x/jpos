package com.jpos.user.repository.implementation;

import com.jpos.user.exception.AdminAlreadyExistsException;
import com.jpos.user.model.AdminUser;
import com.jpos.user.model.LoginUser;
import com.jpos.user.model.User;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.utils.UserBuilder;
import utils.AbstractCsvRepository;
import utils.CsvRepositorySupport;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class FileUserRepository extends AbstractCsvRepository<User> implements UserRepository {
    private static final String DATA_LABEL = "User data";
    private static final String[] HEADER = new String[] {"id", "name", "password", "role"};

    private final ArrayList<User> users = new ArrayList<>();

    public FileUserRepository() {
        this(CsvRepositorySupport.getDefaultDataFilePath("user.csv"));
    }

    public FileUserRepository(Path filePath) {
        super(filePath, DATA_LABEL, HEADER);
        loadUsers();
    }

    @Override
    public boolean addUser(String username, String password, String role)
            throws InstantiationException {
        User user = UserBuilder.createUser(username, password, role);
        if (user == null) {
            throw new InstantiationException("Unable to create user object");
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

    private void loadUsers() {
        users.clear();
        users.addAll(loadAll());
    }

    private void persistUsers() {
        persistAll(users);
    }

    @Override
    protected User mapRowToEntity(String[] row, int lineNumber) {
        User user = UserBuilder.createUser(row[1], row[2], row[3]);
        if (user == null) {
            throw new IllegalArgumentException(String.format("Invalid role '%s'.", row[3]));
        }

        user.setId(UUID.fromString(row[0].trim()));
        return user;
    }

    @Override
    protected String[] mapEntityToRow(User entity) {
        return new String[] {
                entity.getId().toString(),
                entity.getUsername(),
                entity.getPassword(),
                entity.getRole().toLowerCase(Locale.ROOT)
        };
    }

    @Override
    protected int expectedColumnCount() {
        return 4;
    }
}
