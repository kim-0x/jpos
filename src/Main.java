import Repository.Implementation.MockUserRepository;
import Repository.UserRepository;
import Service.Implementation.LoginServiceImpl;
import Service.Implementation.UserServiceImpl;
import Service.LoginService;
import Service.UserService;
import Utils.WelcomeMessage;
import View.AppMenu;
import View.Implementation.AppView;
import View.Implementation.AppMenuView;
import View.Implementation.UserFeatureView;
import View.UserFeature;

void main() {
    UserRepository userRepository = new MockUserRepository();
    LoginService loginService = new LoginServiceImpl(userRepository);
    UserService userService = new UserServiceImpl(userRepository);
    AppMenu appMenuView = new AppMenuView(loginService);
    UserFeature userFeatureView = new UserFeatureView(loginService, userService);
    AppView appView = new AppView(userFeatureView, appMenuView);
    WelcomeMessage.displayWelcomeMessage();
    appView.createSession();
}
