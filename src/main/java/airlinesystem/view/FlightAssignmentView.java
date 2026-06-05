package airlinesystem.view;

import airlinesystem.exceptions.InvalidId;
import airlinesystem.exceptions.InvalidOption;
import airlinesystem.models.FlightAssignment;
import airlinesystem.models.FlightRole;
import airlinesystem.models.Pilot;
import airlinesystem.services.FlightAssignmentService;

import java.util.List;

import static airlinesystem.utils.ReaderUtils.*;
import static airlinesystem.utils.ExitOperationUtil.isExit;

public class FlightAssignmentView {
    private final FlightAssignmentService flightAssignmentService;

    public FlightAssignmentView(FlightAssignmentService flightAssignmentService) {
        this.flightAssignmentService = flightAssignmentService;
    }

    public void run() {
        while (true) {
            showFlightAssignmentMenu();
            try {
                int option = readOption();
                int status = execFlightAssignmentOption(option);
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

    private void showFlightAssignmentMenu() {
        System.out.println("\n-------- Flight Assignment Menu --------");
        System.out.println("1. Assign pilot to flight");
        System.out.println("2. Assign flight attendants to flight");
        System.out.println("3. Remove flight assignment");
        System.out.println("4. List flight assignments");
        System.out.println("5. Check a specific flight assignment");
        System.out.println("9. Back");
    }

    private int execFlightAssignmentOption(int option) throws InvalidId {
        switch (option) {
            case 1:
                assignPilotToFlight();
                break;
            case 2:
                assignFlightAttendantsToFlight();
                break;
            case 3:
                deleteFlightAssignment();
                break;
            case 4:
                listFlightAssignments();
                break;
            case 5:
                checkFlightAssignment();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
        }
        return 0;
    }

    private void assignPilotToFlight() {
        String flightId = readString("Enter flight ID (type 'exit' to cancel): ");
        if (isExit(flightId)) {
            return;
        }
        List<Pilot> availablePilots = flightAssignmentService.getAvailablePilots(flightId);
        if (availablePilots.isEmpty()) {
            System.out.println("No available pilots for the selected flight.");
            return;
        }
        System.out.println("=== Available pilots for flight " + flightId + " === ");
        for (airlinesystem.models.Pilot pilot : availablePilots) {
            System.out.println("- ID: " + pilot.getEmployeeId() + " | Name: " + pilot.getFirstName() + " " + pilot.getLastName());
        }
        System.out.println("==================================================\n");

        String pilotId = readString("Enter pilot ID from the list above (type 'exit' to cancel): ");
        if (isExit(pilotId)) {
            return;
        }

        FlightRole role = readPilotRole();
        if (role == null) {
            return;
        }
        try {
            flightAssignmentService.assignPilotToFlight(pilotId, flightId, role);
            System.out.println("Pilot assigned successfully.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error assigning pilot: " + e.getMessage());
        }
    }

    // each flight must have a pilot, first_officer and multiple flight attendants (2-10)
    private FlightRole readPilotRole() {
        while (true) {
            System.out.println("Available pilot roles:");
            System.out.println("1. CAPTAIN");
            System.out.println("2. FIRST_OFFICER");
            System.out.println("9. Cancel");

            try {
                int option = readOption();
                switch (option) {
                    case 1:
                        return FlightRole.CAPTAIN;
                    case 2:
                        return FlightRole.FIRST_OFFICER;
                    case 9:
                        System.out.println("Assignment canceled.");
                        return null;
                    default:
                        System.out.println("Invalid choice. Please enter a valid option.");
                }
            } catch (InvalidOption e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void assignFlightAttendantsToFlight() {
        String flightId = readString("Enter flight ID (type 'exit' to cancel): ");
        if (isExit(flightId)) {
            return;
        }
        int flightAttendantCount = readInt("Enter number of flight attendants (between 2 to 10): ");

        try {
            flightAssignmentService.assignFAtoFlight(flightId, flightAttendantCount);
            System.out.println("Flight attendants assigned successfully.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error assigning flight attendants: " + e.getMessage());
        }
    }

    // the PK of an assignment is (empId, flightId)
    private String readAssignmentId() {
        String employeePersonId = readString("Enter employee person ID (type 'exit' to cancel): ");
        if (isExit(employeePersonId)) {
            return null;
        }
        String flightId = readString("Enter flight ID (type 'exit' to cancel): ");
        if (isExit(flightId)) {
            return null;
        }
        return employeePersonId + "," + flightId;
    }

    private void deleteFlightAssignment() throws InvalidId {
        String assignmentId = readAssignmentId();
        if (assignmentId == null) {
            return;
        }
        FlightAssignment assignment = flightAssignmentService.getFlightAssignment(assignmentId);

        if (assignment == null) {
            System.out.println("Flight assignment with ID " + assignmentId + " not found.");
            return;
        }
        flightAssignmentService.deleteFlightAssignment(assignmentId);
        System.out.println("Flight assignment deleted successfully.");
    }

    private void listFlightAssignments() {
        List<FlightAssignment> assignments = flightAssignmentService.getAllFlightAssignments();
        if (assignments.isEmpty()) {
            System.out.println("The list is empty. Try adding a flight assignment.");
            return;
        }
        System.out.println("\n================ Registered flight assignments ================");
        for (FlightAssignment assignment : assignments) {
            System.out.println(assignment);
            System.out.println("-------------------------------------------------------");
        }
    }

    private void checkFlightAssignment() throws InvalidId {
        String assignmentId = readAssignmentId();

        FlightAssignment assignment = flightAssignmentService.getFlightAssignment(assignmentId);
        if (assignment == null) {
            System.out.println("Flight assignment with ID " + assignmentId + " not found.");
            return;
        }
        printFlightAssignment(assignment, assignmentId);
    }

    // to print the employee + flight details easier; otherwise would depend on multiple instances of toString of different classes
    private void printFlightAssignment(FlightAssignment assignment, String assignmentId) {
        System.out.println("Flight assignment ID: " + assignmentId);

        if (assignment.getEmployee() != null) {
            System.out.println("Employee ID: " + assignment.getEmployee().getEmployeeId());
            System.out.println("Employee name: " + assignment.getEmployee().getFirstName()
                    + " " + assignment.getEmployee().getLastName());
        }

        if (assignment.getFlight() != null) {
            System.out.println("Flight ID: " + assignment.getFlight().getFlightId());
        }

        System.out.println("Role: " + assignment.getRole());
    }
}