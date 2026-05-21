package com.jpos.user;

import com.jpos.user.exception.UnauthorizedUserActionException;
import com.jpos.user.model.LoginUser;
import com.jpos.user.model.User;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.service.LoginService;
import com.jpos.user.service.UserService;
import com.jpos.user.service.implementation.LoginServiceImpl;
import com.jpos.user.service.implementation.UserServiceImpl;
import com.jpos.user.utils.UserBuilder;
import java.security.InvalidParameterException;
import java.util.Arrays;

public class UserFacade {
    private final LoginService loginService;
    private final UserService userService;
    private final String[] roles = new String[] {"admin", "manager", "cashier"};

    public UserFacade(UserRepository userRepository) {
        loginService = new LoginServiceImpl(userRepository);
        userService = new UserServiceImpl(userRepository);
    }

    public User[] getUsers(String role) {
        return userService.getUsers(role);
    }

    public LoginUser getCurrentUserLogin() {
        return loginService.getCurrentUserLogin();
    }

    public void createNewUser(String username, String password, String role) throws InvalidParameterException,
            InstantiationException {
        if (!Arrays.asList(roles).contains(role.toLowerCase())) {
           throw new InvalidParameterException(String.format("Invalid role %s", role));
        }

        LoginUser currentUser = loginService.getCurrentUserLogin();
        if (currentUser == null) {
            throw new UnauthorizedUserActionException("User is unauthorized access this feature.");
        }

        userService.addUser(username, password, role, currentUser.getRole());
    }

    public boolean signIn(String username, String password) {
        return loginService.signIn(username, password);
    }

    public void signOut() {
        loginService.signOut();
    }

    public String[] getAccessFeatures() throws InstantiationException {
        LoginUser currentUser = this.getCurrentUserLogin();
        User user = UserBuilder.createUser(currentUser.getUsername(), "", currentUser.getRole());
        if (user == null) {
            throw new InstantiationException("Unable to identify user");
        }

        return user.getAccessFeatures();
    }

    public boolean validAccessFeatureKey(int key) throws InstantiationException, IndexOutOfBoundsException {
        String[] allFeatures = this.getAccessFeatures();

        if (key <= 0 || key > allFeatures.length) {
            throw new IndexOutOfBoundsException(String.format("Invalid access key %d.", key));
        }
        return true;
    }
}
