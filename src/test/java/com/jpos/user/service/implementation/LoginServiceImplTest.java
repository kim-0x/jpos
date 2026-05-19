package com.jpos.user.service.implementation;

import com.jpos.user.model.LoginUser;
import com.jpos.user.repository.implementation.MockUserRepository;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.service.LoginService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LoginServiceImplTest {
    private LoginService loginService;

    @Before
    public void setUp() {
        UserRepository userRepository = new MockUserRepository();
        loginService = new LoginServiceImpl(userRepository);
        loginService.signOut();
    }

    @After
    public void tearDown() {
        loginService.signOut();
    }

    @Test
    public void signInReturnsTrueForValidCredentials() {
        boolean result = loginService.signIn("admin", "admin");

        assertTrue(result);
    }

    @Test
    public void signInStoresCurrentUserOnSuccess() {
        loginService.signIn("admin", "admin");

        LoginUser currentUser = loginService.getCurrentUserLogin();
        assertNotNull(currentUser);
        assertEquals("admin", currentUser.getUsername());
        assertEquals("Admin", currentUser.getRole());
    }

    @Test
    public void signInReturnsFalseForInvalidCredentials() {
        boolean result = loginService.signIn("admin", "wrong-password");

        assertFalse(result);
    }

    @Test
    public void signOutClearsCurrentUser() {
        loginService.signIn("admin", "admin");

        loginService.signOut();

        assertNull(loginService.getCurrentUserLogin());
    }
}
