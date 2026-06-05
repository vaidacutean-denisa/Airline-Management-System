package airlinesystem.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import airlinesystem.models.Airport;
import airlinesystem.models.City;
import airlinesystem.services.AuditService;

public class AirportRepository implements GenericRepository<Airport> {
    private final Connection connection;
    private final AuditService auditService;

    // city FK
    private final CityRepository cityRepository;

    public AirportRepository(Connection connection, AuditService auditService, CityRepository cityRepository) {
        this.connection = connection;
        this.auditService = auditService;
        this.cityRepository = cityRepository;
    }

    @Override
    public void add(Airport obj) {
        String query = "INSERT INTO airports (id, name, city_id) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, obj.getId());
            statement.setString(2, obj.getName());
            statement.setString(3, obj.getCity().getId());

            statement.executeUpdate();
            auditService.logAdd("Airport", obj.getId());
        } catch (SQLException e) {
            auditService.logError("AirportRepository " + obj.getId(), e.getMessage());
        }
    }

    @Override
    public Airport get(String id) {
        String query = "SELECT * FROM airports WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String cityId = resultSet.getString("city_id");
                    City city = cityRepository.get(cityId);

                    auditService.logGet("Airport", id);
                    return new Airport(
                            resultSet.getString("id"),
                            resultSet.getString("name"),
                            city
                    );
                }
            }
        } catch (SQLException e) {
            auditService.logError("AirportRepository", e.getMessage());
        }
        return null;
    }

    @Override
    public List<Airport> getAll() {
        List<Airport> airports = new ArrayList<>();
        String query = "SELECT id FROM airports";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Airport airport = get(resultSet.getString("id"));
                if (airport != null) {
                    airports.add(airport);
                }
            }
            auditService.logGet("All airports", "");
        } catch (SQLException e) {
            auditService.logError("AirportRepository", e.getMessage());
        }
        return airports;
    }

    @Override
    public void update(Airport obj) {
        String query = "UPDATE airports SET name = ?, city_id = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, obj.getName());
            statement.setString(2, obj.getCity().getId());
            statement.setString(3, obj.getId());

            statement.executeUpdate();
            auditService.logUpdate("Airport", obj.getId());
        } catch (SQLException e) {
            auditService.logError("AirportRepository", e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        String query = "DELETE FROM airports WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);

            statement.executeUpdate();
            auditService.logDelete("Airport", id);
        } catch (SQLException e) {
            auditService.logError("AirportRepository", e.getMessage());
        }
    }

    public List<Airport> getAirportsByCity(String id) {
        String query = "SELECT * FROM airports WHERE city_id = ?";
        List<Airport> airports = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String airportId = resultSet.getString("id");
                Airport airport = get(airportId);
                airports.add(airport);
            }
            auditService.logGet("Airports by city: ", id);
        } catch (SQLException e) {
            auditService.logError("AirportRepository", e.getMessage());
        }
        return airports;
    }
}