package airlinesystem.utils;

public class ExitOperationUtil {

    public static boolean isExit(String input) {
        if (input != null && "exit".equalsIgnoreCase(input.trim())) {
            System.out.println("Operation cancelled.");
            return true;
        }
        return false;
    }
}
