package airlinesystem.repository;

import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import airlinesystem.models.Airport;
import airlinesystem.models.Route;
import airlinesystem.services.AuditService;

public class RouteRepository implements GenericRepository<Route> {
    private final Connection connection;
    private final AuditService auditService;

    private final AirportRepository airportRepository;

    public RouteRepository(Connection connection, AuditService auditService, AirportRepository airportRepository) {
        this.connection = connection;
        this.auditService = auditService;
        this.airportRepository = airportRepository;
    }

    @Override
    public void add(Route obj) {
        String query = "INSERT INTO routes (id, departure_airport_id, arrival_airport_id, distance_km, estimated_duration_minutes) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, obj.getId());
            statement.setString(2, obj.getDepartureAirport().getId());
            statement.setString(3, obj.getArrivalAirport().getId());
            statement.setDouble(4, obj.getDistanceKm());

            // must convert java duration to minutes (int) for the DB
            int minutes = (int) obj.getEstimatedDuration().toMinutes();
            statement.setInt(5, minutes);

            statement.executeUpdate();
            auditService.logAdd("Route", obj.getId());
        } catch (SQLException e) {
            auditService.logError("RouteRepository " + obj.getId(), e.getMessage());
        }
    }

    @Override
    public Route get(String id) {
        String query = "SELECT * FROM routes WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String depAirportId = resultSet.getString("departure_airport_id");
                    String arrAirportId = resultSet.getString("arrival_airport_id");

                    // double foreign keys corresponding to the departure and arrival airports
                    Airport departureAirport = airportRepository.get(depAirportId);
                    Airport arrivalAirport = airportRepository.get(arrAirportId);

                    // must convert minutes back to duration to be able to properly call the constructor
                    int minutes = resultSet.getInt("estimated_duration_minutes");
                    Duration duration = Duration.ofMinutes(minutes);

                    auditService.logGet("Route", id);
                    return new Route(
                            resultSet.getString("id"),
                            departureAirport,
                            arrivalAirport,
                            resultSet.getDouble("distance_km"),
                            duration
                    );
                }
            }
        } catch (SQLException e) {
            auditService.logError("RouteRepository", e.getMessage());
        }
        return null;
    }

    @Override
    public List<Route> getAll() {
        List<Route> routes = new ArrayList<>();
        String query = "SELECT id FROM routes";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            // for every route id in resultSet, fetch the corresponding object by using get()
            while (resultSet.next()) {
                Route route = get(resultSet.getString("id"));
                routes.add(route);
            }
            auditService.logGet("All routes", "");
        } catch (SQLException e) {
            auditService.logError("RouteRepository", e.getMessage());
        }
        return routes;
    }

    @Override
    public void update(Route obj) {
        String query = "UPDATE routes SET departure_airport_id = ?, arrival_airport_id = ?, " +
                "distance_km = ?, estimated_duration_minutes = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, obj.getDepartureAirport().getId());
            statement.setString(2, obj.getArrivalAirport().getId());
            statement.setDouble(3, obj.getDistanceKm());

            int minutes = (int) obj.getEstimatedDuration().toMinutes();
            statement.setInt(4, minutes);
            statement.setString(5, obj.getId());

            statement.executeUpdate();
            auditService.logUpdate("Route", obj.getId());
        } catch (SQLException e) {
            auditService.logError("RouteRepository", e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        String query = "DELETE FROM routes WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);
            statement.executeUpdate();
            auditService.logDelete("Route", id);
        } catch (SQLException e) {
            auditService.logError("RouteRepository", e.getMessage());
        }
    }

    public enum AirportRole {
        DEPARTURE, ARRIVAL
    }
    public List<Route> getRoutesByAirport(String airport, AirportRole role) {
        String columnName = "";
        if (role == AirportRole.DEPARTURE) {
            columnName = "departure_airport_id";
        } else {
            columnName = "arrival_airport_id";
        }

        String query = "SELECT id FROM routes WHERE " + columnName + " = ?";
        List<Route> routes = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, airport);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Route route = get(resultSet.getString("id"));
                routes.add(route);
            }
            auditService.logGet("Routes by airport", airport);
        } catch (SQLException e) {
            auditService.logError("RouteRepository", e.getMessage());
        }
        return routes;
    }

    public Route findRoute(String departureId, String arrivalId) {
        String query = "SELECT id FROM routes WHERE departure_airport_id = ? AND arrival_airport_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, departureId);
            statement.setString(2, arrivalId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return get(resultSet.getString("id"));
            }
        } catch (SQLException e) {
            auditService.logError("RouteRepository", e.getMessage());
        }
        return null;
    }
}