package com.jpos.user.repository.implementation;

import com.jpos.user.model.AdminUser;
import com.jpos.user.model.LoginUser;
import com.jpos.user.model.User;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.utils.UserBuilder;
import utils.CsvRepositorySupport;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class FileUserRepository implements UserRepository {
    private static final String DATA_LABEL = "User data";
    private static final String[] HEADER = new String[] {"id", "name", "password", "role"};

    private final Path filePath;
    private final ArrayList<User> users = new ArrayList<>();

    public FileUserRepository() {
        this(CsvRepositorySupport.getDefaultDataFilePath("user.csv"));
    }

    public FileUserRepository(Path filePath) {
        this.filePath = filePath;
        loadUsers();
    }

    @Override
    public boolean addUser(String username, String password, String role)
            throws UnsupportedOperationException, InstantiationException {
        User user = UserBuilder.createUser(username, password, role);
        if (user == null) {
            throw new InstantiationException("Unable to create user object");
        }

        if (user instanceof AdminUser) {
            throw new UnsupportedOperationException("Multiple Admin users do not supported yet.");
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

        List<String[]> rows = CsvRepositorySupport.readRows(filePath, DATA_LABEL);
        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
            String[] row = rows.get(rowIndex);
            if (row.length != 4) {
                throw new IllegalStateException(String.format("Invalid user row at line %d.", rowIndex + 1));
            }

            try {
                User user = UserBuilder.createUser(row[1], row[2], row[3]);
                if (user == null) {
                    throw new IllegalArgumentException(String.format("Invalid role '%s'.", row[3]));
                }

                user.setId(UUID.fromString(row[0].trim()));
                users.add(user);
            } catch (RuntimeException exception) {
                throw new IllegalStateException(String.format("Invalid user row at line %d.", rowIndex + 1),
                        exception);
            }
        }
    }

    private void persistUsers() {
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(HEADER);

        for (User user : users) {
            rows.add(new String[] {
                    user.getId().toString(),
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole().toLowerCase(Locale.ROOT)
            });
        }

        CsvRepositorySupport.writeRows(filePath, DATA_LABEL, rows);
    }
}
