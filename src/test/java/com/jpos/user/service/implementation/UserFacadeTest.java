package com.jpos.user.service.implementation;

import com.jpos.user.UserFacade;
import com.jpos.user.exception.AdminAlreadyExistsException;
import com.jpos.user.exception.UnauthorizedUserActionException;
import com.jpos.user.exception.UsernameAlreadyExistsException;
import com.jpos.user.model.AdminUser;
import com.jpos.user.model.LoginUser;
import com.jpos.user.model.User;
import com.jpos.user.repository.implementation.MockUserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class UserFacadeTest {
    private UserFacade userFacade;

    @Before
    public void setUp() throws Exception {
        resetUsers();
        userFacade = new UserFacade(new MockUserRepository());
        userFacade.signOut();
    }

    @After
    public void tearDown() throws Exception {
        userFacade.signOut();
        resetUsers();
    }

    @Test
    public void shouldLoginAdminAndExposeCurrentUser() {
        boolean isSignedIn = userFacade.signIn("admin", "admin");

        assertTrue(isSignedIn);
        LoginUser currentUser = userFacade.getCurrentUserLogin();
        assertNotNull(currentUser);
        assertEquals("admin", currentUser.getUsername());
        assertEquals("Admin", currentUser.getRole());
    }

    @Test
    public void shouldClearCurrentUserAfterLogout() {
        userFacade.signIn("admin", "admin");

        userFacade.signOut();

        assertNull(userFacade.getCurrentUserLogin());
    }

    @Test
    public void shouldReturnFalseForNonExistedUserLogin() {
        boolean isSignedIn = userFacade.signIn("nonExistedUser", "password");

        assertFalse(isSignedIn);
        assertNull(userFacade.getCurrentUserLogin());
    }

    @Test
    public void shouldCreateUserAsAdminAndIncludeItInUserList() throws Exception {
        userFacade.signIn("admin", "admin");

        userFacade.createNewUser("testUser", "password", "cashier");

        User[] users = userFacade.getUsers(userFacade.getCurrentUserLogin().getRole());
        assertTrue(Arrays.stream(users).anyMatch(user ->
                "testUser".equals(user.getUsername()) && "Cashier".equals(user.getRole())));
    }

    @Test
    public void shouldThrowWhenUsernameAlreadyExists() throws Exception {
        userFacade.signIn("admin", "admin");

        assertThrows(UsernameAlreadyExistsException.class,
                () -> userFacade.createNewUser("admin", "password", "cashier"));
    }

    @Test
    public void shouldThrowWhenCreatingSecondAdmin() throws Exception {
        userFacade.signIn("admin", "admin");

        assertThrows(AdminAlreadyExistsException.class,
                () -> userFacade.createNewUser("otherAdmin", "password", "admin"));
    }

    @Test
    public void shouldThrowAccessDeniedWhenCashierGetsUsers() throws Exception {
        signInAsCashier();

        assertThrows(UnauthorizedUserActionException.class,
                () -> userFacade.getUsers(userFacade.getCurrentUserLogin().getRole()));
    }

    @Test
    public void shouldThrowAccessDeniedWhenCashierCreatesUser() throws Exception {
        signInAsCashier();

        assertThrows(UnauthorizedUserActionException.class,
                () -> userFacade.createNewUser("testUser", "password", "cashier"));
    }

    private void signInAsCashier() throws Exception {
        userFacade.signIn("admin", "admin");
        userFacade.createNewUser("cashierUser", "password", "cashier");
        userFacade.signOut();
        assertTrue(userFacade.signIn("cashierUser", "password"));
    }

    @SuppressWarnings("unchecked")
    private void resetUsers() throws Exception {
        Field usersField = MockUserRepository.class.getDeclaredField("users");
        usersField.setAccessible(true);
        ArrayList<User> users = (ArrayList<User>) usersField.get(null);
        users.clear();
        users.add(new AdminUser("admin", "admin"));
    }
}
