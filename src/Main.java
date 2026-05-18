import Repository.Implementation.MockUserRepository;
import Repository.UserRepository;
import Service.Implementation.LoginServiceImpl;
import Service.Implementation.UserServiceImpl;
import Service.LoginService;
import Service.UserService;
import Utils.WelcomeMessage;
import View.AppMenu;
import View.AppMenuView;
import View.UserView;

void main() {
    UserRepository userRepository = new MockUserRepository();
    LoginService loginService = new LoginServiceImpl(userRepository);
    UserService userService = new UserServiceImpl(userRepository);
    AppMenu appMenu = new AppMenuView(loginService);
    UserView userView = new UserView(loginService, userService, appMenu);
    WelcomeMessage.displayWelcomeMessage();
    userView.createSession();
}
