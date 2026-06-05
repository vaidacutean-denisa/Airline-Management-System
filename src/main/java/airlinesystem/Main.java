package airlinesystem;

import airlinesystem.repository.*;
import airlinesystem.services.*;
import airlinesystem.utils.PersonReaderUtils;
import airlinesystem.view.Application;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        AuditService auditService = AuditService.getInstance();
        DatabaseManager databaseManager = DatabaseManager.getInstance();

        try {
            Connection connection = databaseManager.getConnection();

            AirplaneModelRepository airplaneModelRepository = new AirplaneModelRepository(connection, auditService);
            AirplaneModelService airplaneModelService = new AirplaneModelService(airplaneModelRepository);

            AirplaneRepository airplaneRepository = new AirplaneRepository(connection, auditService, airplaneModelRepository);
            AirplaneService airplaneService = new AirplaneService(airplaneRepository, airplaneModelRepository);

            CountryRepository countryRepository = new CountryRepository(connection, auditService);
            CountryService countryService = new CountryService(countryRepository);

            CityRepository cityRepository = new CityRepository(connection, auditService, countryRepository);
            CityService cityService = new CityService(cityRepository, countryRepository);

            AirportRepository airportRepository = new AirportRepository(connection, auditService, cityRepository);
            AirportService airportService = new AirportService(airportRepository, cityService);

            RouteRepository routeRepository = new RouteRepository(connection, auditService, airportRepository);
            RouteService routeService = new RouteService(routeRepository, airportService);

            PassengerRepository passengerRepository = new PassengerRepository(connection, auditService);
            FlightRepository flightRepository = new FlightRepository(connection, auditService, routeRepository, airplaneRepository, passengerRepository);
            BookingRepository bookingRepository = new BookingRepository(connection, auditService, flightRepository, passengerRepository);

            PassengerService passengerService = new PassengerService(passengerRepository, routeService);
            FlightService flightService = new FlightService(flightRepository, routeService, airplaneService, airplaneModelService);
            BookingService bookingService = new BookingService(bookingRepository, flightService, passengerService);

            PilotRepository pilotRepository = new PilotRepository(connection, auditService, countryRepository, airplaneModelRepository);
            PilotService pilotService = new PilotService(pilotRepository, airplaneModelService);

            FlightAttendantRepository flightAttendantRepository = new FlightAttendantRepository(connection, auditService, countryRepository);
            FlightAttendantService flightAttendantService = new FlightAttendantService(flightAttendantRepository);

            CheckInAgentRepository checkInAgentRepository = new CheckInAgentRepository(connection, auditService, countryRepository, airportRepository);
            CheckInAgentService checkInAgentService = new CheckInAgentService(checkInAgentRepository);

            FlightAssignmentRepository flightAssignmentRepository = new FlightAssignmentRepository(connection, auditService, flightRepository, pilotRepository, flightAttendantRepository);
            FlightAssignmentService flightAssignmentService = new FlightAssignmentService(flightAssignmentRepository, flightService, flightAttendantService, pilotService);

            PersonReaderUtils personReaderUtils = new PersonReaderUtils(countryService);
            Application application = new Application(airplaneModelService, airplaneService, countryService, cityService, airportService, routeService, flightService,
                    passengerService, bookingService, pilotService, flightAttendantService, checkInAgentService, flightAssignmentService, personReaderUtils);

            application.run();
            databaseManager.closeConnection();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
