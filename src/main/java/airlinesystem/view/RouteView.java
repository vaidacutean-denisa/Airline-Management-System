package airlinesystem.view;

import airlinesystem.exceptions.InvalidId;
import airlinesystem.exceptions.InvalidOption;
import airlinesystem.models.Airport;
import airlinesystem.models.Route;
import airlinesystem.services.AirportService;
import airlinesystem.services.RouteService;

import java.time.Duration;
import java.util.List;

import static airlinesystem.utils.ReaderUtils.*;
import static airlinesystem.utils.ExitOperationUtil.isExit;

public class RouteView {
    private final RouteService routeService;
    private final AirportService airportService;

    public RouteView(RouteService routeService, AirportService airportService) {
        this.routeService = routeService;
        this.airportService = airportService;
    }

    public void run() {
        while (true) {
            showRouteMenu();
            try {
                int option = readOption();
                int status = execRouteOption(option);
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

    private void showRouteMenu() {
        System.out.println("\n-------- Route Menu --------");
        System.out.println("1. Add route");
        System.out.println("2. Remove route");
        System.out.println("3. List routes");
        System.out.println("4. Check a specific route");
        System.out.println("9. Back");
    }


    private int execRouteOption(int option) throws InvalidId {
        switch (option) {
            case 1:
                addRoute();
                break;
            case 2:
                deleteRoute();
                break;
            case 3:
                listRoutes();
                break;
            case 4:
                checkRoute();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
        }
        return 0;
    }

    public void addRoute() {
        if (airportService.getAllAirports().isEmpty() || airportService.getAllAirports().size() < 2) {
            System.out.println("No airport available. Please add at least two first.");
            return;
        }

        System.out.println("Enter route details (type 'exit' to cancel): ");
        Airport departureAirport = readAirport("Enter departure airport ID: ");
        if (departureAirport == null) {
            return;
        }
        Airport arrivalAirport = readAirport("Enter arrival airport ID: ");
        if (arrivalAirport == null) {
            return;
        }

        if (departureAirport.equals(arrivalAirport)) {
            System.out.println("Departure and arrival airports cannot be the same. Please retry.");
            return;
        }

        String routeId = readString("Enter route ID: ");
        if (isExit(routeId)) {
            return;
        }
        double distanceKm = readDouble("Enter distance in kilometers: ");
        Duration duration = readDuration("Enter estimated route duration in hours: ");

        try {
            Route newRoute = new Route(routeId, departureAirport, arrivalAirport, distanceKm, duration);
            routeService.addRoute(newRoute);
            System.out.println("Route added successfully.");

        } catch (IllegalArgumentException e) {
            System.out.println("Error adding route: " + e.getMessage());
        }
    }

    private Airport readAirport(String input) {
        while (true) {
            String airportId = readString(input);
            if (isExit(airportId)) {
                return null;
            }
            Airport airport = airportService.getAirport(airportId);
            if (airport != null) {
                return airport;
            }
            System.out.println("Invalid airport ID. Please retry or type 'exit' to cancel.");
        }
    }

    private void deleteRoute() throws InvalidId {
        String routeId = readString("Enter route ID (type 'exit' to cancel): ");
        if (isExit(routeId)) {
            return;
        }
        Route route = routeService.getRoute(routeId);
        if (route == null) {
            System.out.println("Route with ID " + routeId + " not found.");
            return;
        }
        routeService.deleteRoute(routeId);
        System.out.println("Route deleted successfully.");
    }

    private void listRoutes() {
        List<Route> routes = routeService.getAllRoutes();
        if (routes.isEmpty()) {
            System.out.println("The list is empty. Try adding a route.");
            return;
        }
        System.out.println("\n================ Registered routes ================");
        for (Route route : routes) {
            System.out.println(route);
            System.out.println("-------------------------------------------------------");
        }
    }

    private void checkRoute() throws InvalidId {
        String routeId = readString("Enter route ID (type 'exit' to cancel): ");
        if (isExit(routeId)) {
            return;
        }
        Route route = routeService.getRoute(routeId);
        if (route == null) {
            System.out.println("Route with ID " + routeId + " not found.");
            return;
        }
        System.out.println(route);
    }
}
