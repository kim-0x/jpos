package com.jpos.user.repository.implementation;

import com.jpos.user.exception.AdminAlreadyExistsException;
import com.jpos.user.exception.UserCreationException;
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
        this(CsvRepositorySupport.getDefaultCsvFilePath("user.csv"));
    }

    public FileUserRepository(Path filePath) {
        super(filePath);
        loadUsers();
    }

    @Override
    public boolean addUser(String username, String password, String role)
            throws UserCreationException {
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
        users.addAll(loadFromCsv());
    }

    private void persistUsers() {
        persistToCsv(users);
    }

    @Override
    protected String getDataLabel() {
        return DATA_LABEL;
    }

    @Override
    protected String[] getHeaderRow() {
        return HEADER;
    }

    @Override
    protected User toEntity(String[] row, int lineNumber) {
        if (row.length != 4) {
            throw new IllegalStateException(String.format("Invalid user row at line %d.", lineNumber));
        }

        try {
            User user = UserBuilder.createUser(row[1], row[2], row[3]);
            if (user == null) {
                throw new IllegalArgumentException(String.format("Invalid role '%s'.", row[3]));
            }

            user.setId(UUID.fromString(row[0].trim()));
            return user;
        } catch (RuntimeException exception) {
            throw new IllegalStateException(String.format("Invalid user row at line %d.", lineNumber), exception);
        }
    }

    @Override
    protected String[] toRow(User user) {
        return new String[] {
                user.getId().toString(),
                user.getUsername(),
                user.getPassword(),
                user.getRole().toLowerCase(Locale.ROOT)
        };
    }
}
