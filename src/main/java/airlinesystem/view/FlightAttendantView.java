package airlinesystem.view;

import airlinesystem.exceptions.InvalidId;
import airlinesystem.exceptions.InvalidOption;
import airlinesystem.models.Country;
import airlinesystem.models.FlightAttendant;
import airlinesystem.models.IdentityDocument;
import airlinesystem.services.FlightAttendantService;
import airlinesystem.utils.PersonReaderUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import static airlinesystem.utils.ReaderUtils.*;
import static airlinesystem.utils.ExitOperationUtil.isExit;

public class FlightAttendantView {
    private final FlightAttendantService flightAttendantService;
    private final PersonReaderUtils personReaderUtils;

    public FlightAttendantView(FlightAttendantService flightAttendantService, PersonReaderUtils personReaderUtils) {
        this.flightAttendantService = flightAttendantService;
        this.personReaderUtils = personReaderUtils;
    }

    void run() {
        while (true) {
            showAttendantMenu();
            try {
                int option = readOption();
                int status = execAttendantOption(option);
                if (status == -1) {
                    break;
                }
            } catch (InvalidOption e) {
                System.out.println(e.getMessage());
            } catch (InvalidId invalidId) {
                System.out.println("Invalid ID.");
            }
        }
    }

    private void showAttendantMenu() {
        System.out.println("\n-------- Flight Attendant Menu --------");
        System.out.println("1. Add flight attendant");
        System.out.println("2. Remove flight attendant");
        System.out.println("3. List flight attendants");
        System.out.println("4. Check a specific flight attendant");
        System.out.println("9. Back");
    }

    private int execAttendantOption(int option) throws InvalidId {
        switch (option) {
            case 1:
                addFlightAttendant();
                break;
            case 2:
                deleteFlightAttendant();
                break;
            case 3:
                listFlightAttendants();
                break;
            case 4:
                checkFlightAttendant();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
        }
        return 0;
    }

    private void addFlightAttendant() {
        System.out.println("Enter flight attendant details: ");
        int personId = readInt("Person ID: ");
        String firstName = readName("First Name (type 'exit' to cancel): ");
        if (isExit(firstName)) {
            return;
        }
        String lastName = readName("Last Name (type 'exit' to cancel): ");
        if (isExit(lastName)) {
            return;
        }
        LocalDate dateOfBirth = readDate("Date of Birth (dd/MM/yyyy): ");
        String email = readEmail("Email: ");
        String phoneNumber = readOptionalString("Phone number (press Enter to skip): ");

        Set<Country> nationalities = personReaderUtils.readNationalities();
        if (nationalities == null) {
            return;
        }
        Set<IdentityDocument> identityDocuments = personReaderUtils.readIdentityDocuments();

        String employeeId = readString("Employee ID: ");
        LocalDate hireDate = readDate("Hire Date (dd/MM/yyyy): ");
        double salary = readDouble("Salary: ");

        List<String> languagesSpoken = readLanguagesSpoken();
        if (languagesSpoken == null) {
            return;
        }

        try {
            FlightAttendant flightAttendant = new FlightAttendant(personId, firstName, lastName, dateOfBirth, nationalities, email,
                    phoneNumber, identityDocuments, employeeId, hireDate, salary, languagesSpoken);

            flightAttendantService.addFlightAttendant(flightAttendant);
            System.out.println("Flight attendant added successfully.");

        } catch (IllegalArgumentException e) {
            System.out.println("Error creating flight attendant: " + e.getMessage());
        }
    }

    private List<String> readLanguagesSpoken() {
        List<String> languages = new ArrayList<>();

        while (true) {
            String lang = readOptionalString("Enter language (type 'exit' to cancel): ");
            if (isExit(lang)) {
                return null;
            }
            if (lang.isBlank()) {
                if (languages.isEmpty()) {
                    System.out.println("The list cannot be empty. Please retry.");                  // must speak at least a language (obviously)
                    continue;
                }
                break;
            }
            if (languages.contains(lang)) {
                System.out.println("The specified language was already added.");
                continue;
            }
            languages.add(lang);
        }
        return languages;
    }


    private void deleteFlightAttendant() throws InvalidId {
        String faID = readString("Enter flight attendant ID (type 'exit' to cancel): ");
        if (isExit(faID)) {
            return;
        }

        FlightAttendant flightAttendant = flightAttendantService.getFlightAttendant(faID);
        if (flightAttendant == null) {
            System.out.println("Flight attendant with ID " + faID + " not found.");
            return;
        }
        flightAttendantService.deleteFlightAttendant(faID);
        System.out.println("Flight attendant deleted successfully.");
    }

    private void listFlightAttendants() {
        List<FlightAttendant> attendants = flightAttendantService.getAllFlightAttendants();
        if (attendants.isEmpty()) {
            System.out.println("The list is empty. Try adding a flight attendant.");
            return;
        }
        System.out.println("\n================ Registered flight attendants ================");
        for (FlightAttendant attendant : attendants) {
            System.out.println(attendant);
            System.out.println("-------------------------------------------------------");
        }
    }

    private void checkFlightAttendant() throws InvalidId {
        String attendantId = readString("Enter flight attendant id: (type 'exit' to cancel)");
        if (isExit(attendantId)) return;

        while (true) {
            FlightAttendant fa = flightAttendantService.getFlightAttendant(attendantId);
            if (fa == null) {
                System.out.println("Flight attendant with id " + attendantId + " not found.");
                return;
            }
            showIdMenu(fa.getEmployeeId());
            try {
                int option = readOption();
                int status = execIdOption(option, fa);
                if (status == -1) {
                    return;
                }
            } catch (InvalidOption e) {
                System.out.println("Invalid option. Please retry.");
            }
        }
    }

    private void showIdMenu(String employeeId) {
        System.out.println("Flight Attendant ID: " + employeeId + " menu");
        System.out.println("1. Show details");
        System.out.println("2. Update first name");
        System.out.println("3. Update last name");
        System.out.println("4. Update email");
        System.out.println("5. Update identity documents");
        System.out.println("9. Exit");
    }

    private int execIdOption(int option, FlightAttendant fa) {
        String personId = String.valueOf(fa.getPersonId());
        switch (option) {
            case 1:
                System.out.println(fa);
                break;
            case 2:
                updateFAFirstName(personId);
                break;
            case 3:
                updateFALastName(personId);
                break;
            case 4:
                updateFAEmail(personId);
                break;
            case 5:
                updateFAIdentityDocuments(personId);
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid option. Please retry.");
        }
        return 0;
    }

    private void updateFAFirstName(String personId) {
        String firstName = readName("Enter first name: ");
        flightAttendantService.updateFAFirstName(personId, firstName);
        System.out.println("First name updated successfully.");
    }

    private void updateFALastName(String personId) {
        String lastName = readName("Enter last name: ");
        flightAttendantService.updateFALastName(personId, lastName);
        System.out.println("Last name updated successfully.");
    }

    private void updateFAEmail(String personId) {
        String email = readEmail("Enter email: ");
        flightAttendantService.updateFAEmail(personId, email);
        System.out.println("Email updated successfully.");
    }

    private void updateFAIdentityDocuments(String personId) {
        Set<IdentityDocument> identityDocuments = personReaderUtils.readIdentityDocuments();
        if (identityDocuments == null) {
            System.out.println("Update canceled: no valid identity documents provided.");
            return;
        }
        try {
            flightAttendantService.updateFAIdDocs(personId, identityDocuments);
            System.out.println("Identity documents updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating identity documents: " + e.getMessage());
        }
    }
}