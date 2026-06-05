package airlinesystem.utils;

import airlinesystem.exceptions.InvalidOption;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class ReaderUtils {
    private static final Scanner scanner = new Scanner(System.in);

    public static int readOption() throws InvalidOption {
        int option = readInt("Enter your option: ");
        if (option >= 1 && option <= 20)
            return option;
        else
            throw new InvalidOption("Invalid option entered. Please choose a number between 1 and 9.");
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                return 0;
            }
            String line = scanner.nextLine().trim();
            if (line.matches("^\\d+$")) {
                return Integer.parseInt(line);
            } else {
                System.out.println("Invalid input. Please enter a valid integer (positive digits only).");
            }
        }
    }

    // for optional attributes (can be blank)
    public static String readOptionalString(String input) {
        System.out.print(input);
        return scanner.nextLine().trim();
    }

    // for required attributes (cannot be blank)
    public static String readString(String input) {
        while (true) {
            System.out.print(input);
            String line = scanner.nextLine().trim();

            if (!line.isBlank()) {
                return line;
            }
            System.out.println("Input cannot be empty. Please enter a valid text.");
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (line.matches("^\\d+(\\.\\d+)?$")) {
                return Double.parseDouble(line);
            } else {
                System.out.println("Invalid input. Please enter a valid decimal number (e.g., 2500 or 12.5).");
            }
        }
    }

    public static LocalDateTime readLocalDateTime(String message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        while (true) {
            System.out.print(message + " (format: dd/MM/yyyy HH:mm): ");
            String input = scanner.nextLine().trim();

            try {
                return LocalDateTime.parse(input, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid input. Please enter a valid date and time in the format dd/MM/yyyy HH:mm (e.g., 01/01/2023 12:30).");
            }
        }
    }

    public static LocalDate readDate(String input) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        while (true) {
            try {
                System.out.print(input);
                String line = scanner.nextLine().trim();

                return LocalDate.parse(line, formatter);

            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please enter a valid date in the format dd/MM/yyyy.");
            }
        }
    }

    public static String readEmail(String input) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";                      // ex. generic_person123@example.com (must have the domain + TLD)
        while (true) {
            System.out.print(input);
            String email = scanner.nextLine().trim();

            if (email.matches(regex)) {
                return email;
            }

            System.out.println("Invalid email format.");
        }
    }

    public static Boolean readBoolean(String input) {
        while (true) {
            System.out.print(input);
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("yes") || response.equals("true")) {
                return true;
            } else if (response.equals("no") || response.equals("false")) {
                return false;
            }

            System.out.println("Invalid input. Please enter 'yes' or 'no'.");
        }
    }

    public static Duration readDuration(String input) {
        while (true) {
            try {
                System.out.print(input);
                String duration = scanner.nextLine().trim();
                return Duration.ofHours(Long.parseLong(duration));

            } catch (NumberFormatException e) {
                System.out.println("Invalid duration format. Please enter a valid number of hours.");
            }
        }
    }

    public static String readName(String prompt) {
        String namePattern = "^[a-zA-ZăâîșțĂÂÎȘȚ\\s-]{2,}$";

        while (true) {
            String input = readString(prompt).trim();

            if (input.matches(namePattern)) {
                return input;
            }

            System.out.println("Invalid input! Names must contain only letters and be at least 2 characters long.");
        }
    }
}
