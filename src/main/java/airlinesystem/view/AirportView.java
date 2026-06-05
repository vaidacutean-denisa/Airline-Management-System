package airlinesystem.view;

import airlinesystem.exceptions.InvalidId;
import airlinesystem.exceptions.InvalidOption;
import airlinesystem.models.Airport;
import airlinesystem.models.City;
import airlinesystem.models.Route;
import airlinesystem.repository.RouteRepository;
import airlinesystem.services.CityService;
import airlinesystem.services.AirportService;
import airlinesystem.services.RouteService;

import java.util.List;

import static airlinesystem.utils.ReaderUtils.*;
import static airlinesystem.utils.ExitOperationUtil.isExit;

public class AirportView {
    private final AirportService airportService;
    private final CityService cityService;
    private final RouteService routeService;

    public AirportView(AirportService airportService, CityService cityService, RouteService routeService) {
        this.airportService = airportService;
        this.cityService = cityService;
        this.routeService = routeService;
    }

    public void run() {
        while (true) {
            showAirportMenu();
            try {
                int option = readOption();
                int status = execAirportOption(option);
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

    private void showAirportMenu() {
        System.out.println("\n-------- Airport Menu --------");
        System.out.println("1. Add airport");
        System.out.println("2. Remove airport");
        System.out.println("3. Update airport");
        System.out.println("4. List airports");
        System.out.println("5. Check a specific airport");
        System.out.println("9. Back");
    }

    private int execAirportOption(int option) throws InvalidId {
        switch (option) {
            case 1:
                addAirport();
                break;
            case 2:
                deleteAirport();
                break;
            case 3:
                updateAirport();
                break;
            case 4:
                listAirports();
                break;
            case 5:
                checkAirport();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
        }
        return 0;
    }

    private void addAirport() {
        System.out.println("Enter airport details: ");
        City city = readCity();
        if (city == null) {
            return;
        }
        String id = readString("Airport IATA code (ID): (e.g. OTP, BGY, LHR)");
        String name = readString("Airport name: ");

        try {
            Airport newAirport = new Airport(id, name, city);
            airportService.addAirport(newAirport);
            System.out.println("Airport added successfully.");

        } catch (IllegalArgumentException e) {
            System.out.println("Failed to add airport: " + e.getMessage());
        }
    }

    private City readCity() {
        if (cityService.getAllCities().isEmpty()) {
            System.out.println("No cities available. Please add a city first.");
            return null;
        }
        while (true) {
            String cityId = readString("Enter city ID: (type 'exit' to cancel)");
            if (isExit(cityId)) {
                return null;
            }
            City city = cityService.getCity(cityId);
            if (city != null) {
                return city;
            }
            System.out.println("Invalid city ID. Please retry.");
        }
    }

    private void deleteAirport() throws InvalidId {
        String airportId = readString("Enter airport ID (type 'exit' to cancel): ");
        if (isExit(airportId)) {
            return;
        }
        Airport airport = airportService.getAirport(airportId);
        if (airport == null) {
            System.out.println("Airport with ID " + airportId + " not found.");
            return;
        }
        airportService.deleteAirport(airportId);
        System.out.println("Airport deleted successfully.");
    }

    private void updateAirport() {
        String id = readString("Enter the airport ID to update (type 'exit' to cancel): ");
        if (isExit(id)) return;

        Airport existingAirport = airportService.getAirport(id);
        if (existingAirport == null) {
            System.out.println("Airport with ID " + id + " not found.");
            return;
        }
        String newName = readString("Enter new airport name (type 'exit' to cancel): ");
        if (isExit(newName))
            return;

        City newCity = readCity();
        if (newCity == null)
            return;

        Airport updatedAirport = new Airport(id, newName, newCity);
        airportService.updateAirport(updatedAirport);
        System.out.println("Airport updated successfully.");
    }

    private void listAirports() {
        List<Airport> airports = airportService.getAllAirports();
        if (airports.isEmpty()) {
            System.out.println("The list is empty. Try adding an airport.");
            return;
        }
        System.out.println("\n================ Registered airports ================");
        for (Airport airport : airports) {
            System.out.println(airport);
            System.out.println("-------------------------------------------------------");
        }
    }

    private void checkAirport() throws InvalidId {
        String airportId = readString("Enter airport ID (type 'exit' to cancel): ");
        if (isExit(airportId)) {
            return;
        }
        Airport airport = airportService.getAirport(airportId);
        if (airport == null) {
            System.out.println("Airport with ID " + airportId + " not found.");
            return;
        }
        while (true) {
            showAirportIdMenu(airport.getId());
            try {
                int option = readOption();
                int status = execAirportIdOptions(option, airport);
                if (status == -1) {
                    return;
                }
            } catch (InvalidOption e) {
                System.out.println("Invalid option. Please retry.");
            }
        }
    }

    private void showAirportIdMenu(String airportId) {
        System.out.println("Airport ID: " + airportId + " menu");
        System.out.println("1. Show airport information");
        System.out.println("2. Show departing routes");
        System.out.println("3. Show arriving routes");
        System.out.println("9. Exit");
    }

    private int execAirportIdOptions(int option, Airport airport) {
        switch (option) {
            case 1:
                System.out.println(airport);
                break;
            case 2:
                List<Route> departingRoutes = routeService.getRoutesByAirport(airport.getId(), RouteRepository.AirportRole.DEPARTURE);
                System.out.println(departingRoutes);
                break;
            case 3:
                List<Route> arrivingRoutes = routeService.getRoutesByAirport(airport.getId(), RouteRepository.AirportRole.ARRIVAL);
                System.out.println(arrivingRoutes);
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid option. Please retry.");
        }
        return 0;
    }
}
