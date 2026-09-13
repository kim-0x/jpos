package com.jpos.bluejay.security;

import com.jpos.user.model.LoginUser;
import com.jpos.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginUser authenticate(String username, String password) {
        if (!userRepository.validUser(username, password)) {
            return null;
        }
        return userRepository.getUserLogin(username);
    }
}
