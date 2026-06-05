package airlinesystem.view;

import airlinesystem.exceptions.InvalidId;
import airlinesystem.exceptions.InvalidOption;
import airlinesystem.models.Pilot;
import airlinesystem.models.Country;
import airlinesystem.models.IdentityDocument;
import airlinesystem.models.AirplaneModel;

import airlinesystem.services.AirplaneModelService;
import airlinesystem.services.PilotService;
import airlinesystem.utils.PersonReaderUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static airlinesystem.utils.ReaderUtils.*;
import static airlinesystem.utils.ExitOperationUtil.isExit;

public class PilotView {
    private final PilotService pilotService;
    private final AirplaneModelService airplaneModelService;
    private final PersonReaderUtils personReaderUtils;

    public PilotView(PilotService pilotService, AirplaneModelService airplaneModelService, PersonReaderUtils personReaderUtils) {
        this.pilotService = pilotService;
        this.airplaneModelService = airplaneModelService;
        this.personReaderUtils = personReaderUtils;
    }

    void run() {
        while (true) {
            showPilotMenu();
            try {
                int option = readOption();
                int status = execPilotOption(option);
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

    private void showPilotMenu() {
        System.out.println("\n-------- Pilot Menu --------");
        System.out.println("1. Add pilot");
        System.out.println("2. Remove pilot");
        System.out.println("3. List pilots");
        System.out.println("4. Check a specific pilot");
        System.out.println("9. Back");
    }

    private int execPilotOption(int option) throws InvalidId {
        switch (option) {
            case 1:
                addPilot();
                break;
            case 2:
                deletePilot();
                break;
            case 3:
                listPilots();
                break;
            case 4:
                checkPilot();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
        }
        return 0;
    }

    private void addPilot() {
        System.out.println("Enter pilot details: ");
        int personId = readInt("Person ID: ");
        String firstName = readName("First Name: (type 'exit' to cancel)");
        if (isExit(firstName)) {
            return;
        }
        String lastName = readName("Last Name: (type 'exit' to cancel)");
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
        if (isExit(employeeId)) {
            return;
        }
        LocalDate hireDate = readDate("Hire Date (dd/MM/yyyy): ");
        double salary = readDouble("Salary: ");
        String licenseNumber = readString("License Number: ");
        Map<AirplaneModel, LocalDate> certifications = readCertifications();

        try {
            Pilot newPilot = new Pilot(personId, firstName, lastName, dateOfBirth, nationalities, email,
                    phoneNumber, identityDocuments, employeeId, hireDate, salary, licenseNumber, certifications);

            pilotService.addPilot(newPilot);
            System.out.println("Pilot added successfully.");

        } catch (IllegalArgumentException e) {
            System.out.println("Error creating pilot: " + e.getMessage());
        }
    }

    private Map<AirplaneModel, LocalDate> readCertifications() {
        Map<AirplaneModel, LocalDate> licenses = new HashMap<>();

        if (airplaneModelService.getAllAirplaneModels().isEmpty()) {
            System.out.println("No airplane models available in the database. Try adding one first.");
            return licenses;
        }

        while (true) {
            String modelId = readOptionalString("Enter an airplane model ID: (or press Enter to exit)");
            if (modelId.isBlank()) {
                return licenses;
            }

            AirplaneModel model = airplaneModelService.getAirplaneModel(modelId);
            if (model == null) {
                System.out.println("The specified airplane model does not exist in the database. Please retry.");
                continue;
            }
            if (licenses.containsKey(model)) {
                System.out.println("This certification was already added to the list.");
                continue;
            }

            LocalDate expiryDate = readDate("Expiration date (dd/MM/yyyy): ");
            licenses.put(model, expiryDate);
            System.out.println("Certification added successfully.");
        }
    }

    private void deletePilot() throws InvalidId {
        String pilotId = readString("Enter pilot id: (type 'exit' to cancel)");
        if (isExit(pilotId)) {
            return;
        }
        Pilot pilot = pilotService.getPilot(pilotId);
        if (pilot == null) {
            System.out.println("Pilot with id " + pilotId + " not found.");
            return;
        }
        pilotService.deletePilot(pilotId);
        System.out.println("Pilot deleted successfully.");
    }

    private void listPilots() {
        List<Pilot> pilots = pilotService.getAllPilots();
        if (pilots.isEmpty()) {
            System.out.println("The list is empty. Try adding a pilot.");
            return;
        }
        System.out.println("\n================ Registered pilots ================");
        for (Pilot pilot : pilots) {
            System.out.println(pilot);
            System.out.println("-------------------------------------------------------");
        }
    }

    private void checkPilot() throws InvalidId {
        String pilotId = readString("Enter pilot id: (type 'exit' to cancel)");
        if (isExit(pilotId)) {
            return;
        }

        while (true) {
            Pilot pilot = pilotService.getPilot(pilotId);
            if (pilot == null) {
                System.out.println("Pilot with id " + pilotId + " not found.");
                return;
            }
            String id = pilot.getEmployeeId();
            showPilotIdMenu(id);
            try {
                int option = readOption();
                int status = execPilotIdOption(option, pilot);
                if (status == -1) {
                    return;
                }
            } catch (InvalidOption e) {
                System.out.println("Invalid option. Please retry.");
            }
        }
    }

    private void showPilotIdMenu(String pilotId) {
        System.out.println("Pilot ID: " + pilotId + " menu");
        System.out.println("1. Show pilot details");
        System.out.println("2. Update pilot certifications");
        System.out.println("3. Update first name");
        System.out.println("4. Update last name");
        System.out.println("5. Update email");
        System.out.println("6. Update identity documents");
        System.out.println("9. Exit");
    }

    private int execPilotIdOption(int option, Pilot pilot) {
        String pilotId = String.valueOf(pilot.getPersonId());
        switch (option) {
            case 1:
                System.out.println(pilot);
                break;
            case 2:
                updatePilotCertifications(pilot);
                break;
            case 3:
                updatePilotFirstName(pilotId);
                break;
            case 4:
                updatePilotLastName(pilotId);
                break;
            case 5:
                updatePilotEmail(pilotId);
                break;
            case 6:
                updatePilotIdentityDocuments(pilotId);
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid option. Please retry.");
        }
        return 0;
    }


    private void updatePilotCertifications(Pilot pilot) {
        if (airplaneModelService.getAllAirplaneModels().isEmpty()) {
            System.out.println("No airplane models available in the database. Try adding one first.");
            return;
        }

        String modelId = readOptionalString("Enter airplane model ID to add/renew (or press Enter to cancel): ");
        if (modelId.isBlank()) {
            System.out.println("Update canceled.");
            return;
        }
        AirplaneModel model = airplaneModelService.getAirplaneModel(modelId);
        if (model == null) {
            System.out.println("The specified airplane model does not exist in the database.");
            return;
        }

        LocalDate expiryDate = readDate("Enter new expiration date (dd/MM/yyyy): ");
        String pilotId = String.valueOf(pilot.getPersonId());
        pilotService.updateCertifications(pilotId, model, expiryDate);
        System.out.println("Certification updated successfully.");
    }

    private void updatePilotFirstName(String pilotId) {
        String firstName = readName("Enter first name: ");
        pilotService.updatePilotFirstName(pilotId, firstName);
        System.out.println("First name updated successfully.");
    }

    private void updatePilotLastName(String pilotId) {
        String lastName = readName("Enter last name: ");
        pilotService.updatePilotLastName(pilotId, lastName);
        System.out.println("Last name updated successfully.");
    }

    private void updatePilotEmail(String pilotId) {
        String email = readEmail("Enter email: ");
        pilotService.updatePilotEmail(pilotId, email);
        System.out.println("Email updated successfully.");
    }

    private void updatePilotIdentityDocuments(String pilotId) {
        Set<IdentityDocument> identityDocuments = personReaderUtils.readIdentityDocuments();
        if (identityDocuments == null) {
            System.out.println("Update canceled: no valid identity documents provided.");
            return;
        }
        try {
            pilotService.updatePilotIdentityDocuments(pilotId, identityDocuments);
            System.out.println("Pilot identity documents updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating identity documents: " + e.getMessage());
        }
    }
}
