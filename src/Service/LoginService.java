package Service;

import Model.LoginUser;

public interface LoginService {
    abstract boolean signIn(String username, String password);
    abstract LoginUser getCurrentUserLogin();
    abstract void signOut();
}
