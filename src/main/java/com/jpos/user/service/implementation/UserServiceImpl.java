package com.jpos.user.service.implementation;

import com.jpos.user.model.User;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.service.UserService;
import utils.IO;

import java.io.InvalidObjectException;
import java.nio.file.AccessDeniedException;
import java.security.InvalidKeyException;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean addUser(String username, String password, String newUserRole, String currentUserRole) throws InvalidKeyException,
            InstantiationException,
            UnsupportedOperationException,
            AccessDeniedException {
        if (userRepository.isNameTaken(username)) {
            throw new InvalidKeyException(String.format("Username %s was taken.", username));
        }

        boolean isAdminRole = currentUserRole.equals("Admin");
        if (!isAdminRole) {
            throw new AccessDeniedException(String.format("Role %s is unauthorized access this feature.", currentUserRole));
        }
        return userRepository.addUser(username, password, newUserRole);
    }

    @Override
    public User[] getUsers(String role) throws AccessDeniedException {
        boolean isAdminRole = role.equals("Admin");
        if (!isAdminRole) {
            throw new AccessDeniedException(String.format("Role %s is unauthorized access this feature.", role));
        }
        return userRepository.getUsers();
    }
}
