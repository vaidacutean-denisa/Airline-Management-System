package airlinesystem.repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import airlinesystem.models.Airplane;
import airlinesystem.models.FlightStatus;
import airlinesystem.models.Flight;
import airlinesystem.models.Route;
import airlinesystem.models.CabinClasses;
import airlinesystem.models.Passenger;
import airlinesystem.models.Booking;
import airlinesystem.services.AuditService;

public class FlightRepository implements GenericRepository<Flight> {
    private final Connection connection;
    private final AuditService auditService;

    private final RouteRepository routeRepository;
    private final AirplaneRepository airplaneRepository;
    private final PassengerRepository passengerRepository;

    public FlightRepository(Connection connection, AuditService auditService, RouteRepository routeRepository, AirplaneRepository airplaneRepository, PassengerRepository passengerRepository) {
        this.connection = connection;
        this.auditService = auditService;
        this.routeRepository = routeRepository;
        this.airplaneRepository = airplaneRepository;
        this.passengerRepository = passengerRepository;
    }

    @Override
    public void add(Flight obj) {
        String query = "INSERT INTO flights (id, route_id, airplane_tail_number, departure_time, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, obj.getFlightId());
            statement.setString(2, obj.getRoute().getId());
            statement.setString(3, obj.getAirplane().getId());

            // must convert LocalDateTime to timestamp for DB
            statement.setTimestamp(4, Timestamp.valueOf(obj.getDepartureTime()));
            statement.setString(5, obj.getStatus().name());

            statement.executeUpdate();
            auditService.logAdd("Flight", obj.getFlightId());
        } catch (SQLException e) {
            auditService.logError("FlightRepository " + obj.getFlightId(), e.getMessage());
        }
    }

    @Override
    public Flight get(String id) {
        String query = "SELECT * FROM flights WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);

            Flight flight = null;

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String routeId = resultSet.getString("route_id");
                    String airplaneTailNumber = resultSet.getString("airplane_tail_number");

                    Route route = routeRepository.get(routeId);
                    Airplane airplane = airplaneRepository.get(airplaneTailNumber);

                    // must convert timestamp back to LocalDateTime
                    LocalDateTime departureTime = resultSet.getTimestamp("departure_time").toLocalDateTime();
                    FlightStatus flightStatus = FlightStatus.valueOf(resultSet.getString("status"));

                    auditService.logGet("Flight", id);
                    flight = new Flight(
                            resultSet.getString("id"),
                            route,
                            airplane,
                            departureTime,
                            flightStatus
                    );
                }
            }

            String selectBookings = "SELECT * FROM bookings WHERE flight_id = ?";

            // if the flight exists in the database, we must add info in the booking set stored in memory (otherwise, we might get an empty set, since the bookings are only stored in the DB)
            if (flight != null) {
                try (PreparedStatement bStatement = connection.prepareStatement(selectBookings)) {
                    bStatement.setString(1, id);

                    try (ResultSet bResultSet = bStatement.executeQuery()) {
                        while (bResultSet.next()) {
                            String ticketId = bResultSet.getString("ticket_id");
                            int passengerId = bResultSet.getInt("passenger_person_id");

                            CabinClasses cabinClass = CabinClasses.valueOf(bResultSet.getString("cabin_class"));
                            double luggageWeight = bResultSet.getDouble("luggage_weight");

                            Passenger passenger = passengerRepository.get(String.valueOf(passengerId));

                            if (passenger != null) {
                                Booking booking = new Booking(
                                        ticketId, flight, passenger, cabinClass, luggageWeight
                                );
                                flight.addBooking(booking);
                            }
                        }
                    }
                }
                auditService.logGet("Flight", id);
                return flight;
            }

        } catch (SQLException e) {
            auditService.logError("FlightRepository", e.getMessage());
        }
        return null;
    }

    @Override
    public List<Flight> getAll() {
        List<Flight> flights = new ArrayList<>();
        String query = "SELECT * FROM flights";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            // instead of using the n+1 selects method (antipattern), we iterate through the resultSet and reconstruct each flight object (better performance)
            while (resultSet.next()) {
                String routeId = resultSet.getString("route_id");
                String airplaneTailNumber = resultSet.getString("airplane_tail_number");

                Route route = routeRepository.get(routeId);
                Airplane airplane = airplaneRepository.get(airplaneTailNumber);

                LocalDateTime departureTime = resultSet.getTimestamp("departure_time").toLocalDateTime();
                FlightStatus flightStatus = FlightStatus.valueOf(resultSet.getString("status"));

                Flight flight = new Flight(
                        resultSet.getString("id"),
                        route,
                        airplane,
                        departureTime,
                        flightStatus
                );
                flights.add(flight);
            }
            auditService.logGet("All flights", "");
        } catch (SQLException e) {
            auditService.logError("FlightRepository", e.getMessage());
        }
        return flights;
    }

    @Override
    public void update(Flight obj) {
        String query = "UPDATE flights SET route_id = ?, airplane_tail_number = ?, " +
                "departure_time = ?, status = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, obj.getRoute().getId());
            statement.setString(2, obj.getAirplane().getId());
            statement.setTimestamp(3, Timestamp.valueOf(obj.getDepartureTime()));
            statement.setString(4, obj.getStatus().name());
            statement.setString(5, obj.getFlightId());

            statement.executeUpdate();
            auditService.logUpdate("Flight", obj.getFlightId());
        } catch (SQLException e) {
            auditService.logError("FlightRepository", e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        String query = "DELETE FROM flights WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);
            statement.executeUpdate();
            auditService.logDelete("Flight", id);
        } catch (SQLException e) {
            auditService.logError("FlightRepository", e.getMessage());
        }
    }
}