package airlinesystem.repository;

import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import airlinesystem.models.City;
import airlinesystem.models.Country;
import airlinesystem.services.AuditService;

public class CityRepository implements GenericRepository<City> {
    private final Connection connection;
    private final AuditService auditService;

    // to access the country object associated with the city
    private final CountryRepository countryRepository;

    public CityRepository(Connection connection, AuditService auditService, CountryRepository countryRepository) {
        this.connection = connection;
        this.auditService = auditService;
        this.countryRepository = countryRepository;
    }

    @Override
    public void add(City obj) {
        String query = "INSERT INTO cities (id, name, country_iso_code, timezone) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, obj.getId());
            statement.setString(2, obj.getName());
            statement.setString(3, obj.getCountry().getId());
            statement.setString(4, obj.getTimezone().getId());                  // ZoneId returns the name of the timezone (ex: "Europe/Bucharest")

            statement.executeUpdate();
            auditService.logAdd("City", obj.getId());
        } catch (SQLException e) {
            auditService.logError("CityRepository " + obj.getId(), e.getMessage());
        }
    }

    @Override
    public City get(String id) {
        String query = "SELECT * FROM cities WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String countryIsoCode = resultSet.getString("country_iso_code");

                    // country FK; we store the country object instead of its name (easier access to it throughout the app; otherwise would have to chain multiple methods)
                    Country country = countryRepository.get(countryIsoCode);

                    auditService.logGet("City", id);
                    return new City(
                            resultSet.getString("id"),
                            resultSet.getString("name"),
                            country,
                            ZoneId.of(resultSet.getString("timezone"))
                    );
                }
            }
        } catch (SQLException e) {
            auditService.logError("CityRepository", e.getMessage());
        }
        return null;
    }

    @Override
    public List<City> getAll() {
        List<City> cities = new ArrayList<>();
        String query = "SELECT id FROM cities";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                City city = get(resultSet.getString("id"));
                if (city != null) {
                    cities.add(city);
                }
            }
            auditService.logGet("All cities", "");
        } catch (SQLException e) {
            auditService.logError("CityRepository", e.getMessage());
        }
        return cities;
    }

    @Override
    public void update(City obj) {
        String query = "UPDATE cities SET name = ?, country_iso_code = ?, timezone = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, obj.getName());
            statement.setString(2, obj.getCountry().getId());
            statement.setString(3, obj.getTimezone().getId());
            statement.setString(4, obj.getId());

            statement.executeUpdate();
            auditService.logUpdate("City", obj.getId());
        } catch (SQLException e) {
            auditService.logError("CityRepository", e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        String query = "DELETE FROM cities WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);

            statement.executeUpdate();
            auditService.logDelete("City", id);
        } catch (SQLException e) {
            auditService.logError("CityRepository", e.getMessage());
        }
    }

    public List<City> getCitiesByCountry(String isoCode) {
        String query = "SELECT * FROM cities WHERE country_iso_code = ?";
        List<City> cities = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, isoCode);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String cityId = resultSet.getString("id");
                City city = get(cityId);

                if (city != null) {
                    cities.add(city);
                }
            }
        } catch (SQLException e) {
            auditService.logError("CityRepository", e.getMessage());
        }
        return cities;
    }
}