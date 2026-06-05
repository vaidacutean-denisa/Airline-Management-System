package airlinesystem.repository;

import airlinesystem.models.AirplaneModel;
import airlinesystem.models.CabinClasses;
import airlinesystem.services.AuditService;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AirplaneModelRepository implements GenericRepository<AirplaneModel> {
    private final Connection connection;
    private final AuditService auditService;

    public AirplaneModelRepository(Connection connection, AuditService auditService) {
        this.connection = connection;
        this.auditService = auditService;
    }

    @Override
    public void add(AirplaneModel obj) {
        // the airplane_models table cannot store a map of capacities per cabin classes; therefore, we use an auxiliary table (cabin_capacities) to identify each combination of airplane model + cabin class + capacity (which must be inserted as well)
        String queryModel = "INSERT INTO airplane_models (id, model_name, manufacturer, fuel_capacity, cruise_speed, " +
                "max_range, fuel_consumption, maintenance_cycles, cargo_capacity) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";          // "?" represents a placeholder

        String queryCapacity = "INSERT INTO cabin_capacities (model_id, cabin_class, capacity) VALUES (?, ?, ?)";
        try {
            // to treat both statements as a single transaction
            connection.setAutoCommit(false);

            // insert the main info about the airplane model
            try (PreparedStatement statement = connection.prepareStatement(queryModel)) {
                statement.setString(1, obj.getId());
                statement.setString(2, obj.getModelName());
                statement.setString(3, obj.getManufacturer());
                statement.setDouble(4, obj.getFuelCapacity());
                statement.setDouble(5, obj.getCruiseSpeed());
                statement.setDouble(6, obj.getMaxRange());
                statement.setDouble(7, obj.getFuelConsumptionPerKm());
                statement.setInt(8, obj.getMaintenanceCycles());
                statement.setDouble(9, obj.getCargoCapacity());

                statement.executeUpdate();
            }

            // insert the elements of the map (cabin class: capacity)
            try (PreparedStatement statement = connection.prepareStatement(queryCapacity)) {
                for (Map.Entry<CabinClasses, Integer> entry : obj.getCabinCapacity().entrySet()) {
                    statement.setString(1, obj.getId());
                    statement.setString(2, entry.getKey().name());                              // stores the name of the enum as text
                    statement.setInt(3, entry.getValue());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            // save changes in the DB
            connection.commit();
            auditService.logAdd("Airplane Model", obj.getId());

        } catch (SQLException e) {
            try {
                connection.rollback();                                  // rollback any changes so we won't be left with incomplete data
            } catch (SQLException exc) {
                auditService.logError("AirplaneModelRepository", exc.getMessage());
            }
            auditService.logError("AirplaneModelRepository", e.getMessage());
        } finally {                                                     // the block is executed regardless of whether an exception was thrown or not
            try {                                                       // the DB must be returned to its original state (commits automatically)
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                auditService.logError("AirplaneModelRepository", e.getMessage());
            }
        }
    }

    @Override
    public AirplaneModel get(String id) {
        String queryModel = "SELECT * FROM airplane_models WHERE id = ?";
        String queryCapacity = "SELECT cabin_class, capacity FROM cabin_capacities WHERE model_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(queryModel)) {
            // replaces the placeholder with the given id; the statement becomes "SELECT... WHERE id = {given_id}" (a valid query); note: the parameter list starts at index 1
            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                // we have at most one result -> we check if it exists (since the cursor starts before the first object, we check by using .next)
                if (resultSet.next()) {
                    // must read the capacity map of the airplane model before calling its constructor (must get the values associated with it)
                    Map<CabinClasses, Integer> capacities = new HashMap<>();

                    try (PreparedStatement statementCapacity = connection.prepareStatement(queryCapacity)) {
                        statementCapacity.setString(1, id);

                        try (ResultSet rsCapacity = statementCapacity.executeQuery()) {
                            while (rsCapacity.next()) {
                                CabinClasses cls = CabinClasses.valueOf(rsCapacity.getString("cabin_class"));
                                int cap = rsCapacity.getInt("capacity");
                                capacities.put(cls, cap);
                            }
                        }
                    }
                    auditService.logGet("Airplane model", id);

                    // the object is created with all the fetched data == from DB to java (RAM)
                    return new AirplaneModel(
                            resultSet.getString("id"),
                            resultSet.getString("model_name"),
                            resultSet.getString("manufacturer"),
                            resultSet.getDouble("fuel_capacity"),
                            resultSet.getDouble("cruise_speed"),
                            resultSet.getDouble("max_range"),
                            resultSet.getDouble("fuel_consumption"),
                            resultSet.getInt("maintenance_cycles"),
                            capacities,                                                         // we pass the capacity map to the constructor
                            resultSet.getDouble("cargo_capacity")
                    );
                }
            }
        } catch (SQLException e) {
            auditService.logError("AirplaneModelRepository", e.getMessage());
        }
        return null;
    }

    @Override
    public List<AirplaneModel> getAll() {
        List<AirplaneModel> models = new ArrayList<>();
        String query = "SELECT id FROM airplane_models";

        // try-with-resources; any resources declared in the try block are automatically closed after the block completes; note: this is valid only for variables declared in "()"
        try (PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()) {

            // the resultSet iterates through the objects; its initial position is before the first object (index 0; the first obj has index 1)
            while (resultSet.next()) {
                AirplaneModel model = get(resultSet.getString("id"));
                models.add(model);
            }
            auditService.logGet("All airplane models", "");
        } catch (SQLException e) {
            auditService.logError("AirplaneModelRepository", e.getMessage());
        }
        return models;
    }

    @Override
    public void update(AirplaneModel obj) {
        String query = "UPDATE airplane_models SET model_name = ?, manufacturer = ?, fuel_capacity = ?, " +
                "cruise_speed = ?, max_range = ?, fuel_consumption = ?, maintenance_cycles = ?, cargo_capacity = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, obj.getModelName());
            statement.setString(2, obj.getManufacturer());
            statement.setDouble(3, obj.getFuelCapacity());
            statement.setDouble(4, obj.getCruiseSpeed());
            statement.setDouble(5, obj.getMaxRange());
            statement.setDouble(6, obj.getFuelConsumptionPerKm());
            statement.setInt(7, obj.getMaintenanceCycles());
            statement.setDouble(8, obj.getCargoCapacity());
            statement.setString(9, obj.getId());

            statement.executeUpdate();
            auditService.logUpdate("Airplane Model", obj.getId());
        } catch (SQLException e) {
            auditService.logError("AirplaneModelRepository", e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        String query = "DELETE FROM airplane_models WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);
            statement.executeUpdate();
            auditService.logDelete("Airplane Model", id);
        } catch (SQLException e) {
            auditService.logError("AirplaneModelRepository", e.getMessage());
        }
    }

    public List<AirplaneModel> getByManufacturer(String manufacturer) {
        String query = "SELECT * FROM airplane_models WHERE manufacturer = ?";
        List<AirplaneModel> models = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, manufacturer);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String modelId = resultSet.getString("id");

                AirplaneModel model = get(modelId);
                models.add(model);
            }
            auditService.logGet("Airplane models by manufacturer", manufacturer);
        } catch (SQLException e) {
            auditService.logError("AirplaneModelRepository", e.getMessage());
        }
        return models;
    }

    public List<AirplaneModel> getModelsByRange(double minRange) {
        String query = "SELECT * FROM airplane_models WHERE max_range >= ?";
        List<AirplaneModel> models = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, minRange);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String modelId = resultSet.getString("id");

                AirplaneModel model = get(modelId);
                models.add(model);
            }
            auditService.logGet("Airplane models by range", String.valueOf(minRange));
        } catch (SQLException e) {
            auditService.logError("AirplaneModelRepository", e.getMessage());
        }
        return models;
    }
}
