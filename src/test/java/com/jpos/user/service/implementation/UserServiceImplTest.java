package com.jpos.user.service.implementation;

import com.jpos.user.model.User;
import com.jpos.user.repository.implementation.MockUserRepository;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.service.UserService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserServiceImplTest {
    private UserService userService;

    @Before
    public void setUp() {
        UserRepository userRepository = new MockUserRepository();
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    public void addUserReturnsFalseWhenUsernameIsTaken() {
        boolean result = userService.addUser("admin", "admin", "admin");

        assertFalse(result);
    }

    @Test
    public void getUsersReturnsNullWhenRoleIsNotAdmin() {
        User[] users = userService.getUsers("Cashier");

        assertNull(users);
    }

    @Test
    public void getUsersReturnsUsersWhenRoleIsAdmin() {
        User[] users = userService.getUsers("Admin");

        assertNotNull(users);
        assertTrue(users.length >= 1);
    }
}
