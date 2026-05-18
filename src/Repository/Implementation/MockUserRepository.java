package Repository.Implementation;

import Model.*;
import Repository.UserRepository;
import Utils.UserBuilder;

import java.util.ArrayList;
import java.util.List;

public class MockUserRepository implements UserRepository {
    private static final ArrayList<User> users = new ArrayList<>(List.of(new AdminUser("admin", "admin")));

    @Override
    public boolean addUser(String username, String password, String role) {
        User user =  UserBuilder.createUser(username, password, role);
        if (user == null) {
            IO.println("ERROR: Wrong role!");
            return false;
        }

        if (user instanceof AdminUser) {
            IO.println("Multiple Admin users do not allow.");
            return false;
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
                    &&  user.getPassword().equals(password)) {
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
