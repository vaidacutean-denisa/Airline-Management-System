package airlinesystem.view;

import airlinesystem.exceptions.InvalidId;
import airlinesystem.exceptions.InvalidOption;
import airlinesystem.models.CheckInAgent;
import airlinesystem.models.Country;
import airlinesystem.models.Airport;
import airlinesystem.models.IdentityDocument;
import airlinesystem.services.AirportService;
import airlinesystem.services.CheckInAgentService;
import airlinesystem.utils.PersonReaderUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static airlinesystem.utils.ReaderUtils.*;
import static airlinesystem.utils.ExitOperationUtil.isExit;

public class CheckInAgentView {
    public final CheckInAgentService agentService;
    public final AirportService airportService;
    public final PersonReaderUtils personReaderUtils;

    public CheckInAgentView(CheckInAgentService agentService, AirportService airportService, PersonReaderUtils personReaderUtils) {
        this.agentService = agentService;
        this.airportService = airportService;
        this.personReaderUtils = personReaderUtils;
    }

    void run() {
        while (true) {
            showAgentMenu();
            try {
                int option = readOption();
                int status = execAgentOption(option);
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

    private void showAgentMenu() {
        System.out.println("\n-------- Check-in Agent Menu --------");
        System.out.println("1. Add check-in agent");
        System.out.println("2. Remove check-in agent");
        System.out.println("3. List check-in agents");
        System.out.println("4. Check a specific check-in agent");
        System.out.println("9. Back");
    }

    private int execAgentOption(int option) throws InvalidId {
        switch (option) {
            case 1:
                addAgent();
                break;
            case 2:
                deleteAgent();
                break;
            case 3:
                listAgents();
                break;
            case 4:
                checkAgent();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
        }
        return 0;
    }


    private void addAgent() {
        System.out.println("Enter check-in agent details: ");
        int personId = readInt("Person ID: ");
        Airport airport = readAssignedAirport();
        if (airport == null) {
            return;
        }

        String firstName = readName("First Name (type 'exit' to cancel): ");
        if (isExit(firstName)) {
            return;
        }
        String lastName = readName("Last Name: ");
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
            CheckInAgent agent = new CheckInAgent(personId, firstName, lastName, dateOfBirth, nationalities, email,
                    phoneNumber, identityDocuments, employeeId, hireDate, salary, languagesSpoken, airport);

            agentService.addCheckInAgent(agent);
            System.out.println("Check-in agent added successfully.");

        } catch (IllegalArgumentException e) {
            System.out.println("Error creating check-in agent: " + e.getMessage());
        }
    }

    private Airport readAssignedAirport() {
        if (airportService.getAllAirports().isEmpty()) {
            System.out.println("No airport available. Try adding one first.");
            return null;
        }
        while (true) {
            String airportId = readString("Enter assigned airport ID (type 'exit' to cancel): ");
            if (isExit(airportId)) {
                return null;
            }
            Airport airport = airportService.getAirport(airportId);
            if (airport != null) {
                return airport;
            }
            System.out.println("Airport not found. Please try again.");
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

    private void deleteAgent() throws InvalidId {
        String agentID = readString("Enter check-in agent ID (type 'exit' to cancel): ");
        if (isExit(agentID)) {
            return;
        }
        CheckInAgent agent = agentService.getCheckInAgent(agentID);
        if (agent == null) {
            System.out.println("Check-in agent with ID " + agentID + " not found.");
            return;
        }
        agentService.deleteCheckInAgent(agentID);
        System.out.println("Check-in agent deleted successfully.");
    }

    private void listAgents() {
        List<CheckInAgent> agents = agentService.getAllCheckInAgents();
        if (agents.isEmpty()) {
            System.out.println("The list is empty. Try adding a check-in agent.");
            return;
        }
        System.out.println("\n================ Registered check-in agents ================");
        for (CheckInAgent agent : agents) {
            System.out.println(agent);
            System.out.println("-------------------------------------------------------");
        }
    }


    private void checkAgent() throws InvalidId {
        String agentId = readString("Enter check-in agent id: (type 'exit' to cancel)");
        if (isExit(agentId)) return;

        while (true) {
            CheckInAgent agent = agentService.getCheckInAgent(agentId);
            if (agent == null) {
                System.out.println("Check-in agent with id " + agentId + " not found.");
                return;
            }
            showIdMenu(agent.getEmployeeId());
            try {
                int option = readOption();
                int status = execIdOption(option, agent);
                if (status == -1) {
                    return;
                }
            } catch (InvalidOption e) {
                System.out.println("Invalid option. Please retry.");
            }
        }
    }

    private void showIdMenu(String employeeId) {
        System.out.println("Check-In Agent ID: " + employeeId + " menu");
        System.out.println("1. Show details");
        System.out.println("2. Update first name");
        System.out.println("3. Update last name");
        System.out.println("4. Update email");
        System.out.println("5. Update identity documents");
        System.out.println("6. Update assigned airport");
        System.out.println("9. Exit");
    }

    private int execIdOption(int option, CheckInAgent agent) {
        String personId = String.valueOf(agent.getPersonId());
        switch (option) {
            case 1:
                System.out.println(agent);
                break;
            case 2:
                updateAgentFirstName(personId);
                break;
            case 3:
                updateAgentLastName(personId);
                break;
            case 4:
                updateAgentEmail(personId);
                break;
            case 5:
                updateAgentIdDocs(personId);
                break;
            case 6:
                updateAgentAirport(personId);
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid option. Please retry.");
        }
        return 0;
    }

    private void updateAgentFirstName(String personId) {
        String firstName = readName("Enter first name: ");
        agentService.updateCheckInAgentFirstName(personId, firstName);
        System.out.println("First name updated successfully.");
    }

    private void updateAgentLastName(String personId) {
        String lastName = readName("Enter last name: ");
        agentService.updateCheckInAgentLastName(personId, lastName);
        System.out.println("Last name updated successfully.");
    }

    private void updateAgentEmail(String personId) {
        String email = readEmail("Enter email: ");
        agentService.updateCheckInAgentEmail(personId, email);
        System.out.println("Email updated successfully.");
    }

    private void updateAgentIdDocs(String personId) {
        Set<IdentityDocument> identityDocuments = personReaderUtils.readIdentityDocuments();
        if (identityDocuments == null) {
            System.out.println("Update canceled: no valid identity documents provided.");
            return;
        }
        try {
            agentService.updateCheckInAgentIdentityDocuments(personId, identityDocuments);
            System.out.println("Identity documents updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating identity documents: " + e.getMessage());
        }
    }

    private void updateAgentAirport(String personId) {
        Airport newAirport = readAssignedAirport();
        if (newAirport == null) {
            System.out.println("Update canceled.");
            return;
        }

        try {
            agentService.updateCheckInAgentAirport(personId, newAirport.getId());
            System.out.println("Assigned airport updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating airport: " + e.getMessage());
        }
    }
}
