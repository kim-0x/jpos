package Service;

import Model.User;

public interface UserService {
    abstract boolean addUser(String username, String password, String role);
    abstract User[] getUsers(String role);
}
