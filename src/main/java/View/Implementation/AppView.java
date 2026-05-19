package View.Implementation;

import Utils.IO;
import Utils.WelcomeMessage;
import View.AppMenu;
import View.UserFeature;

public class AppView {
    private final UserFeature userFeature;
    private final AppMenu appMenu;

    public AppView(UserFeature userFeature, AppMenu appMenu) {
        this.userFeature = userFeature;
        this.appMenu = appMenu;
    }

    public void createSession() {
        userFeature.loginForm();
        int option = -1;
        while (true) {
            int selectedOption = option;
            if (option == -1) {
                selectedOption = appMenu.selectAppMenu();
            }

            if (selectedOption != -1) {
                userFeature.selectFeatureOption(selectedOption);
            }

            var choice = IO.readln("Select an option to start or type 'quit', 'logout' to exit main menu: ");
            if (choice.equals("logout")) {
                userFeature.logoutSession();
                WelcomeMessage.displayWelcomeMessage();
                userFeature.loginForm();
                option = -1;
                continue;
            }

            if (choice.equalsIgnoreCase("quit")) {
                return;
            }

            option = Integer.parseInt(choice);
        }
    }
}
