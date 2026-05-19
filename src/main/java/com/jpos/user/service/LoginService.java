package com.jpos.user.service;

import com.jpos.user.model.LoginUser;

public interface LoginService {
    /**
     * INTENT: Authenticate a user with the supplied credentials and start the current login session.
     * PRECONDITION: username and password are provided, and the backing user repository is available.
     * RETURNS: true when the credentials match an existing user; otherwise false.
     * POSTCONDITION: the current logged-in user is stored when authentication succeeds; otherwise the
     * login state is left unchanged.
     */
    boolean signIn(String username, String password);
    LoginUser getCurrentUserLogin();
    void signOut();
}