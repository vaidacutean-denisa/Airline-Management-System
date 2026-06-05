package airlinesystem.view;

import airlinesystem.exceptions.InvalidId;
import airlinesystem.exceptions.InvalidOption;
import airlinesystem.models.Passenger;
import airlinesystem.models.IdentityDocument;
import airlinesystem.models.Country;
import airlinesystem.services.PassengerService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static airlinesystem.utils.ReaderUtils.*;
import airlinesystem.utils.PersonReaderUtils;
import static airlinesystem.utils.ExitOperationUtil.isExit;

public class PassengerView {
    private final PassengerService passengerService;
    private final PersonReaderUtils personReaderUtils;

    public PassengerView(PassengerService passengerService, PersonReaderUtils personReaderUtils) {
        this.passengerService = passengerService;
        this.personReaderUtils = personReaderUtils;
    }

    public void run() {
        while (true) {
            showPassengerMenu();
            try {
                int option = readOption();
                int status = execPassengerOption(option);
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

    private void showPassengerMenu() {
        System.out.println("\n-------- Passenger Menu --------");
        System.out.println("1. Add passenger");
        System.out.println("2. Remove passenger");
        System.out.println("3. List passengers");
        System.out.println("4. Check a specific passenger");
        System.out.println("9. Back");
    }

    private int execPassengerOption(int option) throws InvalidId {
        switch (option) {
            case 1:
                addPassenger();
                break;
            case 2:
                deletePassenger();
                break;
            case 3:
                listPassengers();
                break;
            case 4:
                checkPassenger();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
        }
        return 0;
    }

    private void addPassenger() {
        System.out.println("Enter passenger details: ");
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
        Boolean needsAssistance = readBoolean("Needs assistance? (yes/no):");

        Set<Country> nationalities = personReaderUtils.readNationalities();
        if (nationalities == null) {
            return;
        }
        Set<IdentityDocument> identityDocuments = personReaderUtils.readIdentityDocuments();

        try {
            Passenger passenger = new Passenger(personId, firstName, lastName, dateOfBirth, nationalities
                    , email, phoneNumber, identityDocuments, needsAssistance);
            passengerService.addPassenger(passenger);
            System.out.println("Passenger added successfully.");

        } catch (IllegalArgumentException e) {
            System.out.println("Error adding passenger: " + e.getMessage());
        }
    }

    private void deletePassenger() throws InvalidId {
        int passengerId = readInt("Enter passenger ID: ");
        Passenger passenger = passengerService.getPassenger(passengerId);
        if (passenger == null) {
            System.out.println("Passenger with ID " + passengerId + " not found.");
            return;
        }
        passengerService.deletePassenger(passengerId);
        System.out.println("Passenger deleted successfully.");
    }

    private void listPassengers() {
        List<Passenger> passengers = passengerService.getAllPassengers();
        if (passengers.isEmpty()) {
            System.out.println("The list is empty. Try adding a passenger.");
            return;
        }
        System.out.println("\n================ Registered passengers ================");
        for (Passenger p : passengers) {
            System.out.println(p);
            System.out.println("-------------------------------------------------------");
        }
    }

    private void checkPassenger() throws InvalidId {
        int passengerId = readInt("Enter passenger ID: ");
        while (true) {
            Passenger passenger = passengerService.getPassenger(passengerId);
            if (passenger == null) {
                System.out.println("Passenger with ID " + passengerId + " not found.");
                return;
            }
            passengerId = passenger.getPersonId();
            showPassengerIdMenu(passengerId);
            try {
                int option = readOption();
                int status = execPassengerIdOption(option, passenger);
                if (status == -1) {
                    return;
                }
            } catch (InvalidOption e) {
                System.out.println("Invalid option. Please retry.");
            }
        }
    }

    private void showPassengerIdMenu(int passengerId) {
        System.out.println("Passenger ID: " + passengerId + " menu");
        System.out.println("1. Show passenger details");
        System.out.println("2. Update passenger information");
        System.out.println("9. Exit");
    }

    private int execPassengerIdOption(int option, Passenger passenger) {
        switch (option) {
            case 1:
                System.out.println(passenger);
                break;
            case 2:
                updatePassengerInfo(passenger);
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
                default:
                    System.out.println("Invalid option. Please retry.");
        }
        return 0;
    }

    private void updatePassengerInfo(Passenger passenger) {
        while (true) {
            showUpdateMenu(passenger.getPersonId());
            try {
                int option = readOption();
                int status = execPassengerUpdate(option, passenger);
                if (status == -1) {
                    return;
                }
            } catch (InvalidOption e) {
                System.out.println("Invalid option. Please retry.");
            }
        }
    }

    private void showUpdateMenu(int passengerId) {
        System.out.println("Passenger update menu");
        System.out.println("1. Update first name");
        System.out.println("2. Update last name");
        System.out.println("3. Update nationalities");
        System.out.println("4. Update phone number");
        System.out.println("5. Update email");
        System.out.println("6. Update identity documents");
        System.out.println("7. Update assistance needs");
        System.out.println("9. Exit");
    }

    private int execPassengerUpdate(int option, Passenger passenger) {
        int passengerId = passenger.getPersonId();
        switch (option) {
            case 1:
                String firstName = readName("Enter first name: ");
                passengerService.updatePassengerFirstName(passengerId, firstName);
                break;
            case 2:
                String lastName = readName("Enter last name: ");
                passengerService.updatePassengerLastName(passengerId, lastName);
                break;
            case 3:
                updatePassengerNationalities(passengerId);
                break;
            case 4:
                String input = readString("Enter phone number: ");
                passengerService.updatePassengerPhoneNumber(passengerId, input);
                break;
            case 5:
                String email = readString("Enter email: ");
                passengerService.updatePassengerEmail(passengerId, email);
                break;
            case 6:
                updatePassengerIdentityDocuments(passengerId);
                break;
            case 7:
                updatePassengerAssistanceNeeds(passengerId);
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid option. Please retry.");
                return -1;
        }
        return 0;
    }

    private void updatePassengerNationalities(int passengerId) {
        Set<Country> nationalities = personReaderUtils.readNationalities();
        if (nationalities == null) {
            return;
        }
        try {
            passengerService.updatePassengerNationalities(passengerId, nationalities);
            System.out.println("Passenger nationalities updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating nationalities: " + e.getMessage());
        }
    }

    private void updatePassengerIdentityDocuments(int passengerId) {
        Set<IdentityDocument> identityDocuments = personReaderUtils.readIdentityDocuments();
        if (identityDocuments == null) {
            System.out.println("Update canceled: no valid identity documents provided.");
            return;
        }
        try {
            passengerService.updatePassengerIdentityDocuments(passengerId, identityDocuments);
            System.out.println("Passenger identity documents updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating identity documents: " + e.getMessage());
        }
    }

    private void updatePassengerAssistanceNeeds(int passengerId) {
        Boolean needsAssistance = readBoolean("Needs assistance? (yes/no): ");
        try {
            passengerService.updatePassengerAssistanceNeeds(passengerId, needsAssistance);
            System.out.println("Passenger assistance needs updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating assistance needs: " + e.getMessage());
        }
    }

}
