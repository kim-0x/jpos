package Service;

import Model.User;

public interface UserService {
    /**
     * INTENT: Create a new user account for the requested role.
     * PRECONDITION: username, password, and role are provided, and the username is not already taken.
     * RETURNS: true when the user is accepted and persisted; otherwise false.
     * POSTCONDITION: a new user is added to the repository on success; otherwise no user is created.
     */
    boolean addUser(String username, String password, String role);

    /**
     * INTENT: Retrieve the list of user accounts visible to the current caller.
     * PRECONDITION: role identifies the currently logged-in user's role.
     * RETURNS: an array of users for authorized access, or null when the caller is not allowed to view
     * user accounts.
     * POSTCONDITION: no repository data is modified.
     */
    User[] getUsers(String role);
}