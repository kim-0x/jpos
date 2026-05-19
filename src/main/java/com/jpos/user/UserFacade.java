package com.jpos.user;

import com.jpos.user.model.LoginUser;
import com.jpos.user.model.User;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.service.LoginService;
import com.jpos.user.service.UserService;
import com.jpos.user.service.implementation.LoginServiceImpl;
import com.jpos.user.service.implementation.UserServiceImpl;

public class UserFacade {
    private final LoginService loginService;
    private final UserService userService;

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

    public boolean addUser(String username, String password, String role) {
        return userService.addUser(username, password, role);
    }

    public boolean signIn(String username, String password) {
        return loginService.signIn(username, password);
    }

    public void signOut() {
        loginService.signOut();
    }
}
