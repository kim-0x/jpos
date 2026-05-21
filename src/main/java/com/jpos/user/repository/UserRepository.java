package com.jpos.user.repository;

import com.jpos.user.model.LoginUser;
import com.jpos.user.model.User;

public interface UserRepository {
    abstract boolean addUser(String username, String password, String role) throws InstantiationException;
    abstract boolean isNameTaken(String name);
    abstract boolean validUser(String username, String password);
    abstract LoginUser getUserLogin(String username);
    abstract User[] getUsers();
}
