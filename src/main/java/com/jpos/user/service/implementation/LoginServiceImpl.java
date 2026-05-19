package com.jpos.user.service.implementation;

import com.jpos.user.model.LoginUser;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.service.LoginService;

public class LoginServiceImpl implements LoginService {
    private final UserRepository userRepository;
    private static LoginUser currentUserLogin;

    public LoginServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean signIn(String username, String password) {
        if (this.userRepository.validUser(username, password)) {
            currentUserLogin = this.userRepository.getUserLogin(username);
            return true;
        }
        return false;
    }

    @Override
    public LoginUser getCurrentUserLogin() {
        return currentUserLogin;
    }

    @Override
    public void signOut() {
        currentUserLogin = null;
    }
}
