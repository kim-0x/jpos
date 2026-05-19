package Utils;

public final class WelcomeMessage {
    private WelcomeMessage() {
    }

    public static void displayWelcomeMessage() {
        System.out.printf("%s%n", "*".repeat(42));
        System.out.printf("%-10s %31s%n", "*", "*");
        System.out.printf("%-10s %-20s %10s%n", "*", "Welcome to JPOS!", "*");
        System.out.printf("%-10s %31s%n", "*", "*");
        System.out.printf("%s%n", "*".repeat(42));
        IO.println("Please login to continue.");
    }
}
