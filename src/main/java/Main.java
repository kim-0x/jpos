import com.jpos.user.UserFacade;
import com.jpos.user.repository.implementation.MockUserRepository;
import com.jpos.user.repository.UserRepository;
import com.jpos.user.service.implementation.LoginServiceImpl;
import com.jpos.user.service.implementation.UserServiceImpl;
import com.jpos.user.service.LoginService;
import com.jpos.user.service.UserService;
import utils.WelcomeMessage;
import view.AppMenu;
import view.implementation.AppMenuView;
import view.implementation.AppView;
import view.implementation.UserFeatureView;
import view.UserFeature;

public class Main {
    public static void main(String[] args) {
        UserRepository userRepository = new MockUserRepository();
        UserFacade userFacade = new UserFacade(userRepository);
        AppMenu appMenuView = new AppMenuView(userFacade);
        UserFeature userFeatureView = new UserFeatureView(userFacade);
        AppView appView = new AppView(userFeatureView, appMenuView);
        WelcomeMessage.displayWelcomeMessage();
        appView.createSession();
    }
}
