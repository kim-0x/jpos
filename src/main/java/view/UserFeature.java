package view;

import com.jpos.user.model.UserRole;

public interface UserFeature {
    void loginForm();
    void logoutSession();
    void createNewUser();
    void displayUsers();
    UserRole getCurrentUserRole();
}
