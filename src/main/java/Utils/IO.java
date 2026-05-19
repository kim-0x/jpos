package Utils;

import java.util.Scanner;

public final class IO {
    private static final Scanner SCANNER = new Scanner(System.in);

    private IO() {
    }

    public static String readln(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine();
    }

    public static void println(String message) {
        System.out.println(message);
    }
}
