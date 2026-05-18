import Model.LoginUser;
import Repository.Implementation.MockUserRepository;
import Repository.UserRepository;
import Service.Implementation.LoginServiceImpl;
import Service.Implementation.UserServiceImpl;
import Service.LoginService;
import Service.UserService;
import View.AppMenuView;
import View.UserView;

void main() {
    UserRepository userRepository = new MockUserRepository();
    LoginService loginService = new LoginServiceImpl(userRepository);
    UserService userService = new UserServiceImpl(userRepository);
    UserView userView = new UserView(loginService, userService);
    displayWelcomeMessage();
    userView.loginForm();
    int option = -1;
    while (true) {
        createSession(loginService, userView, option);
        var choice = IO.readln("Select an option to start or type 'quit', 'logout' to exit main menu: ");
        if (choice.equals("logout")) {
            IO.println("Logout...");
            loginService.signOut();
            clear();
            displayWelcomeMessage();
            userView.loginForm();
            option = -1;
            continue;
        }

        if (choice.equalsIgnoreCase("quit")) {
            break;
        }

        option = Integer.parseInt(choice);
    }
}

private void createSession(LoginService loginService, UserView userView, int option) {
    int selectedOption = option;
    if (option == -1) {
        AppMenuView appMenuView = new AppMenuView(loginService);
        selectedOption = appMenuView.selectAppMenu();
    }

    LoginUser currentLoginUser = loginService.getCurrentUserLogin();
    String userRole = currentLoginUser.getRole();
    // turn on/off features
    if (userRole.equalsIgnoreCase("Admin")) {
        switch(selectedOption) {
            case 1:
                userView.createNewUser();
                break;

            case 2:
                userView.displayUsers();
                break;
        }
    } else {
        IO.println("Feature is not implemented yet.");
    }
}

private void displayWelcomeMessage() {
    System.out.printf("%s%n", "*".repeat(42));
    System.out.printf("%-10s %31s%n", "*", "*");
    System.out.printf("%-10s %-20s %10s%n", "*", "Welcome to JPOS!", "*");
    System.out.printf("%-10s %31s%n", "*", "*");
    System.out.printf("%s%n", "*".repeat(42));
    IO.println("Please login to continue.");
}

private void clear() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
}
