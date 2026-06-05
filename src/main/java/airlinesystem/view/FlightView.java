package airlinesystem.view;

import airlinesystem.exceptions.InvalidId;
import airlinesystem.exceptions.InvalidOption;
import airlinesystem.models.Flight;
import airlinesystem.models.Route;
import airlinesystem.models.Airplane;
import airlinesystem.models.FlightStatus;
import airlinesystem.services.AirplaneService;
import airlinesystem.services.FlightService;
import airlinesystem.services.RouteService;

import java.time.LocalDateTime;
import java.util.List;

import static airlinesystem.utils.ExitOperationUtil.isExit;
import static airlinesystem.utils.ReaderUtils.*;

public class FlightView {
    private final FlightService flightService;
    private final AirplaneService airplaneService;
    private final RouteService routeService;

    public FlightView(FlightService flightService, AirplaneService airplaneService, RouteService routeService) {
        this.flightService = flightService;
        this.airplaneService = airplaneService;
        this.routeService = routeService;
    }

    public void run() {
        while (true) {
            showFlightMenu();
            try {
                int option = readOption();
                int status = execFlightOption(option);
                if (status == -1) {
                    break;
                }
            } catch (InvalidOption e) {
                System.out.println(e.getMessage());
            } catch (InvalidId invalidId) {
                System.out.println("Invalid id.");
            }
        }
    }

    private void showFlightMenu() {
        System.out.println("\n-------- Flight Menu --------");
        System.out.println("1. Add flight");
        System.out.println("2. Remove flight");
        System.out.println("3. List flights");
        System.out.println("4. Check a specific flight");
        System.out.println("9. Back");
    }

    private int execFlightOption(int option) throws InvalidId {
        switch (option) {
            case 1:
                addFlight();
                break;
            case 2:
                deleteFlight();
                break;
            case 3:
                listFlights();
                break;
            case 4:
                checkFlight();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
        }
        return 0;
    }

    private void addFlight() {
        System.out.println("Enter route details: ");

        Route route = readRoute();
        if (route == null) {
            return;
        }
        String routeId = route.getId();
        List<Airplane> validAirplanes = flightService.getValidAirplanes(routeId);
        Airplane airplane = readAirplane(validAirplanes);
        if (airplane == null) {
            return;
        }
        String flightId = readString("Enter flight ID (type 'exit' to cancel): ");
        if (isExit(flightId)) {
            return;
        }
        LocalDateTime departureTime = readLocalDateTime("Enter departure time: ");

        try {
            Flight newFlight = new Flight(flightId, route, airplane, departureTime, FlightStatus.SCHEDULED);
            flightService.addFlight(newFlight);
            System.out.println("Flight added successfully.");

        } catch (IllegalArgumentException e) {
            System.out.println("Error adding flight: " + e.getMessage());
        }
    }

    private Route readRoute() {
        if (routeService.getAllRoutes().isEmpty()) {
            System.out.println("No routes available. Try adding one first.");
            return null;
        }
        while (true) {
            String routeId = readString("Enter route id (or type 'exit' to cancel): ");
            if (isExit(routeId)) {
                return null;
            }
            Route route = routeService.getRoute(routeId);
            if (route != null) {
                return route;
            }
            System.out.println("Invalid route id. Please retry.");
        }
    }

    private Airplane readAirplane(List<Airplane> validAirplanes) {
        if (validAirplanes.isEmpty()) {
            System.out.println("No airplanes meet the technical requirements to fly this route's distance.");
            return null;
        }
        System.out.println("Available airplanes for this route: ");
        for (Airplane airplane: validAirplanes) {
            System.out.println("Airplane tail number: " + airplane.getId());
        }

        while (true) {
            String option = readString("Select airplane tail number (type 'exit' to cancel): ");
            if (isExit(option)) {
                return null;
            }
            Airplane airplane = airplaneService.getAirplane(option);

            if (airplane != null && validAirplanes.contains(airplane)) {
                return airplane;
            }
            System.out.println("Invalid or incompatible airplane. Please retry.");
        }
    }

    private void deleteFlight() throws InvalidId {
        String flightId = readString("Enter flight ID (or type 'exit' to cancel): ");
        if (isExit(flightId)) {
            return;
        }

        Flight flight = flightService.getFlight(flightId);
        if (flight == null) {
            System.out.println("Flight with ID " + flightId + " not found.");
            return;
        }
        flightService.deleteFlight(flightId);
        System.out.println("Flight deleted successfully.");
    }

    private void listFlights() {
        List<Flight> flights = flightService.getAllFlights();
        if (flights.isEmpty()) {
            System.out.println("The list is empty. Try adding a flight.");
            return;
        }
        System.out.println("\n================ Registered flights ================");
        for (Flight flight : flights) {
            System.out.println(flight);
            System.out.println("-------------------------------------------------------");
        }
    }

    private void checkFlight() throws InvalidId {
        String flightId = readString("Enter flight ID (type 'exit' to cancel): ");
        if (isExit(flightId)) {
            return;
        }

        Flight flight = flightService.getFlight(flightId);
        if (flight == null) {
            System.out.println("Flight with ID " + flightId + " not found.");
            return;
        }
        while (true) {
            showFlightIdMenu(flight.getFlightId());
            try {
                int option = readOption();
                int status = execFlightIdOptions(option, flight);
                if (status == -1)
                    return;
            } catch (InvalidOption e) {
                System.out.println("Invalid option selected. Please try again.");
            }
        }
    }

    private void showFlightIdMenu(String flightId) {
        System.out.println("Flight ID: " + flightId + " menu");
        System.out.println("1. Show flight details");
        System.out.println("2. Authorize departure");
        System.out.println("3. Confirm arrival");
        System.out.println("4. Cancel flight");
        System.out.println("5. Show passenger manifest");
        System.out.println("9. Exit");
    }

    private int execFlightIdOptions(int option, Flight flight) throws InvalidOption {
        try {
            String flightId = flight.getFlightId();
            switch (option) {
                case 1:
                    System.out.println(flight);
                    break;
                case 2:
                    flightService.departFlight(flightId);
                    System.out.println("Departure authorized for flight " + flight.getFlightId());
                    break;
                case 3:
                    flightService.confirmFlightArrival(flightId);
                    System.out.println("Arrival confirmed for flight " + flight.getFlightId());
                    break;
                case 4:
                    flightService.cancelFlight(flightId);
                    System.out.println("Flight " + flight.getFlightId() + " cancelled successfully");
                    break;
                case 5:
                    showPassengersManifest(flightId);
                    break;
                case 9:
                    System.out.println("Exiting..");
                    return -1;
                default:
                    throw new InvalidOption("Invalid option selected. Please try again.");
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Error processing flight operation: " + e.getMessage());
        }
        return 0;
    }

    private void showPassengersManifest(String flightId) {
        List<airlinesystem.models.Passenger> passengers = flightService.getPassengerList(flightId);

        if (passengers.isEmpty()) {
            System.out.println("No passengers registered for flight " + flightId);
            return;
        }

        System.out.println("\n======= Passenger Manifest for Flight " + flightId + " =======");
        for (airlinesystem.models.Passenger passenger : passengers) {
            System.out.println(passenger.getLastName() + " " + passenger.getFirstName() + " | ID: " + passenger.getPersonId());
            System.out.println("-----------------------------------------------------------------------");
        }
    }
}
