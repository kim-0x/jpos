package View;

public interface AppMenu {
    /**
     * INTENT: Display the current user's application menu and collect a menu selection.
     * PRECONDITION: a user is logged in and the implementation can resolve that user's available
     * features.
     * RETURNS: the selected menu option number, or -1 when the user quits or access cannot be resolved.
     * POSTCONDITION: no application state is modified beyond any output needed to show the menu.
     */
    int selectAppMenu();
}