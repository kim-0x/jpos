package com.jpos.user.service.implementation;

import com.jpos.user.exception.UnauthorizedUserActionException;
import com.jpos.user.exception.UserCreationException;
import com.jpos.user.exception.UsernameAlreadyExistsException;
import com.jpos.user.model.User;
import com.jpos.user.model.UserRole;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.service.UserService;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean addUser(String username, String password, String newUserRole, String currentUserRole)
            throws UserCreationException {
        if (userRepository.isNameTaken(username)) {
            throw new UsernameAlreadyExistsException(username);
        }

        boolean isAdminRole = currentUserRole.equals(UserRole.ADMIN.getValue());
        if (!isAdminRole) {
            throw new UnauthorizedUserActionException(
                    String.format("Role %s is unauthorized access this feature.", currentUserRole));
        }
        return userRepository.addUser(username, password, newUserRole);
    }

    @Override
    public User[] getUsers(String role) {
        boolean isAdminRole = role.equals(UserRole.ADMIN.getValue());
        if (!isAdminRole) {
            throw new UnauthorizedUserActionException(
                    String.format("Role %s is unauthorized access this feature.", role));
        }
        return userRepository.getUsers();
    }
}
