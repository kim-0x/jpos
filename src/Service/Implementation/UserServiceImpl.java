package Service.Implementation;

import Model.User;
import Repository.UserRepository;
import Service.UserService;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean addUser(String username, String password, String role) {
        if (userRepository.isNameTaken(username)) {
            return false;
        }

        return userRepository.addUser(username, password, role);
    }

    @Override
    public User[] getUsers(String role) {
        boolean isAdminRole = role.equals("Admin");
        if (!isAdminRole) {
            // Should throw un-authorization exception here
            IO.println("Un-authorized access");
            return null;
        }
        return userRepository.getUsers();
    }
}
