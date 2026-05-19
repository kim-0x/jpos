package com.jpos.user.service.implementation;

import com.jpos.user.model.User;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.service.UserService;
import utils.IO;

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
            IO.println("Un-authorized access");
            return null;
        }
        return userRepository.getUsers();
    }
}
