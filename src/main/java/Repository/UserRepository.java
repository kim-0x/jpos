package Repository;

import Model.LoginUser;
import Model.User;

public interface UserRepository {
    abstract boolean addUser(String username, String password, String role);
    abstract boolean isNameTaken(String name);
    abstract boolean validUser(String username, String password);
    abstract LoginUser getUserLogin(String username);
    abstract User[] getUsers();
}
