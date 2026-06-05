package airlinesystem.view;

import airlinesystem.exceptions.InvalidOption;
import airlinesystem.services.*;
import static airlinesystem.utils.ReaderUtils.*;
import airlinesystem.utils.PersonReaderUtils;


public class Application {
    private final AirplaneModelService airplaneModelService;
    private final AirplaneService airplaneService;

    private final CountryService countryService;
    private final CityService cityService;
    private final AirportService airportService;
    private final RouteService routeService;

    private final FlightService flightService;
    private final PassengerService passengerService;
    private final BookingService bookingService;

    private final PilotService pilotService;
    private final FlightAttendantService flightAttendantService;
    private final CheckInAgentService checkInAgentService;

    private final FlightAssignmentService flightAssignmentService;
    private final PersonReaderUtils personReaderUtils;

    public Application(AirplaneModelService airplaneModelService, AirplaneService airplaneService, CountryService countryService, CityService cityService,
                       AirportService airportService, RouteService routeService, FlightService flightService, PassengerService passengerService,
                       BookingService bookingService, PilotService pilotService, FlightAttendantService flightAttendantService, CheckInAgentService checkInAgentService,
                       FlightAssignmentService flightAssignmentService, PersonReaderUtils personReaderUtils) {

        this.airplaneModelService = airplaneModelService;
        this.airplaneService = airplaneService;
        this.countryService = countryService;
        this.cityService = cityService;
        this.airportService = airportService;
        this.routeService = routeService;
        this.flightService = flightService;
        this.passengerService = passengerService;
        this.bookingService = bookingService;
        this.pilotService = pilotService;
        this.flightAttendantService = flightAttendantService;
        this.checkInAgentService = checkInAgentService;
        this.flightAssignmentService = flightAssignmentService;
        this.personReaderUtils = personReaderUtils;
    }

    public void run() {
        while (true) {
            this.showMenu();
            try {
                int option = readOption();
                int status = execute(option);
                if (status == -1) {
                    break;
                }
            } catch (InvalidOption invalidOption) {
                System.out.println("Invalid option selected.");
            }
        }
    }

    public void showMenu() {
        System.out.println("\n========= Airline Management System - Main menu ==========");
        System.out.println("1. Fleet & Logistics");
        System.out.println("2. Route network");
        System.out.println("3. Flight operations");
        System.out.println("4. Customers & Human Resources");
        System.out.println("9. Exit");
    }

    private int execute(int option) {
        switch (option) {
            case 1:
                runLogistics();
                break;
            case 2:
                runNetwork();
                break;
            case 3:
                runOperations();
                break;
            case 4:
                runHumanResources();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid option. Please try again.");
        }
        return 0;
    }

    private void runLogistics() {
        while (true) {
            this.showLogistics();
            try {
                int option = readOption();
                int status = executeLogistics(option);
                if (status == -1) {
                    break;
                }
            } catch (InvalidOption invalidOption) {
                System.out.println("Invalid option selected.");
            }
        }
    }

    private void runNetwork() {
        while (true) {
            this.showNetwork();
            try {
                int option = readOption();
                int status = executeNetwork(option);
                if (status == -1) {
                    break;
                }
            } catch (InvalidOption invalidOption) {
                System.out.println("Invalid option selected.");
            }
        }
    }

    private void runOperations() {
        while (true) {
            this.showOperations();
            try {
                int option = readOption();
                int status = executeOperations(option);
                if (status == -1) {
                    break;
                }
            } catch (InvalidOption invalidOption) {
                System.out.println("Invalid option selected.");
            }
        }
    }

    private void runHumanResources() {
        while (true) {
            this.showHumanResources();
            try {
                int option = readOption();
                int status = executeHR(option);
                if (status == -1) {
                    break;
                }
            } catch (InvalidOption invalidOption) {
                System.out.println("Invalid option selected.");
            }
        }
    }

    private void showLogistics() {
        System.out.println("\n------ Fleet & Logistics menu -------");
        System.out.println("1. Manage airplane models");
        System.out.println("2. Manage airplanes");
        System.out.println("9. Return to main menu");
    }

    private void showNetwork() {
        System.out.println("\n------ Route network menu ------");
        System.out.println("1. Manage countries");
        System.out.println("2. Manage cities");
        System.out.println("3. Manage airports");
        System.out.println("4. Manage routes");
        System.out.println("9. Return to main menu");
    }

    private void showOperations() {
        System.out.println("\n------- Flight operations menu ------");
        System.out.println("1. Manage flights");
        System.out.println("2. Manage bookings");
        System.out.println("9. Return to main menu");
    }

    private void showHumanResources() {
        System.out.println("\n------ Customers & Human Resources menu ------");
        System.out.println("1. Manage passengers");
        System.out.println("2. Manage employees");
        System.out.println("9. Return to main menu");
    }

    private int executeLogistics(int option) {
        switch (option) {
            case 1:
                manageAirplaneModels();
                break;
            case 2:
                manageAirplanes();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid option. Please try again.");
        }
        return 0;
    }

    private int executeNetwork(int option) {
        switch (option) {
            case 1:
                manageCountries();
                break;
            case 2:
                manageCities();
                break;
            case 3:
                manageAirports();
                break;
            case 4:
                manageRoutes();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid option. Please try again.");
        }
        return 0;
    }

    private int executeOperations(int option) {
        switch (option) {
            case 1:
                manageFlights();
                break;
            case 2:
                manageBookings();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid option. Please try again.");
        }
        return 0;
    }

    private int executeHR(int option) {
        switch (option) {
            case 1:
                managePassengers();
                break;
            case 2:
                manageEmployees();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid option. Please try again.");
        }
        return 0;
    }


    private void manageAirplaneModels() {
        AirplaneModelView view = new AirplaneModelView(airplaneModelService);
        view.run();
    }

    private void manageAirplanes() {
        AirplaneView view = new AirplaneView(airplaneService, airplaneModelService);
        view.run();
    }

    private void manageCountries() {
        CountryView view = new CountryView(countryService, cityService);
        view.run();
    }

    private void manageCities() {
        CityView view = new CityView(cityService, countryService, airportService);
        view.run();
    }

    private void manageAirports() {
        AirportView view = new AirportView(airportService, cityService, routeService);
        view.run();
    }

    private void manageRoutes() {
        RouteView view = new RouteView(routeService, airportService);
        view.run();
    }

    private void manageFlights() {
        FlightView view = new FlightView(flightService, airplaneService, routeService);
        view.run();
    }

    private void managePassengers() {
        PassengerView view = new PassengerView(passengerService, personReaderUtils);
        view.run();
    }

    private void manageBookings() {
        BookingView view = new BookingView(bookingService, flightService, passengerService);
        view.run();
    }

    private void manageEmployees() {
        while (true) {
            this.showEmployeesMenu();
            try {
                int option = readOption();
                int status = executeEmployeeOption(option);
                if (status == -1) {
                    break;
                }
            } catch (InvalidOption invalidOption) {
                System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void showEmployeesMenu() {
        System.out.println("\nEmployee Management Menu:");
        System.out.println("1. Manage pilots");
        System.out.println("2. Manage flight attendants");
        System.out.println("3. Manage check-in agents");
        System.out.println("4. Manage flight assignments");
        System.out.println("9. Return to main menu");
    }

    private int executeEmployeeOption(int option) {
        switch (option) {
            case 1:
                managePilots();
                break;
            case 2:
                manageFlightAttendants();
                break;
            case 3:
                manageCheckInAgents();
                break;
            case 4:
                manageFlightAssignments();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid option");
        }
        return 0;
    }

    private void managePilots() {
        PilotView view = new PilotView(pilotService, airplaneModelService, personReaderUtils);
        view.run();
    }

    private void manageFlightAttendants() {
        FlightAttendantView view = new FlightAttendantView(flightAttendantService, personReaderUtils);
        view.run();
    }

    private void manageCheckInAgents() {
        CheckInAgentView view = new CheckInAgentView(checkInAgentService, airportService, personReaderUtils);
        view.run();
    }

    private void manageFlightAssignments() {
        FlightAssignmentView view = new FlightAssignmentView(flightAssignmentService);
        view.run();
    }
}
