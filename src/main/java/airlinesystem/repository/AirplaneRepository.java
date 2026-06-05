package airlinesystem.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import airlinesystem.models.Airplane;
import airlinesystem.models.AirplaneModel;
import airlinesystem.models.AirplaneStatus;
import airlinesystem.services.AuditService;

public class AirplaneRepository implements GenericRepository<Airplane> {
    private final Connection connection;
    private final AuditService auditService;

    // the airplane model is stored as a varchar in the DB, but we need to retrieve it from the AirplaneModelRepository to get the object itself (associated with the airplane)
    private final AirplaneModelRepository modelRepository;

    public AirplaneRepository(Connection connection, AuditService auditService, AirplaneModelRepository airplaneModelRepository) {
        this.connection = connection;
        this.auditService = auditService;
        this.modelRepository = airplaneModelRepository;
    }

    @Override
    public void add(Airplane obj) {
        String query = "INSERT INTO airplanes (tail_number, model_id, pressurization_cycles, last_revision_cycles, airplane_status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, obj.getId());

            // airplane has a FK to airplane_models, stored as a varchar (model_id); getAirplaneModel returns an AirplaneModel, so we must call getId on it
            statement.setString(2, obj.getAirplaneModel().getId());
            statement.setInt(3, obj.getPressurizationCycles());
            statement.setInt(4, obj.getLastRevisionCycles());
            statement.setString(5, obj.getAirplaneStatus().toString());
            statement.executeUpdate();

            auditService.logAdd("Airplane", obj.getId());
        } catch (SQLException e) {
            auditService.logError("AirplaneRepository" + obj.getId(), e.getMessage());
        }
    }

    @Override
    public Airplane get(String id) {
        String query = "SELECT * FROM airplanes WHERE tail_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String modelId = resultSet.getString("model_id");

                    AirplaneModel model = modelRepository.get(modelId);
                    auditService.logGet("Airplane", id);

                    return new Airplane(
                            resultSet.getString("tail_number"),
                            model,
                            resultSet.getInt("pressurization_cycles"),
                            resultSet.getInt("last_revision_cycles"),
                            AirplaneStatus.valueOf(resultSet.getString("airplane_status"))
                    );
                }
            }
        } catch (SQLException e) {
            auditService.logError("AirplaneRepository", e.getMessage());
        }
        return null;
    }

    @Override
    public List<Airplane> getAll() {
        List<Airplane> airplanes = new ArrayList<>();
        String query = "SELECT tail_number FROM airplanes";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            // not the best approach regarding performance: see "n+1 select problem" (the app executes one initial query to fetch parent, followed by N additional queries to fetch child entities)
             while (resultSet.next()) {                                                                 // ^ but it should do the work for a relatively small DB
                 Airplane airplane = get(resultSet.getString("tail_number"));
                 if (airplane != null) {
                     airplanes.add(airplane);
                 }
             }
             auditService.logGet("All airplanes", "");
        } catch (SQLException e) {
            auditService.logError("AirplaneRepository", e.getMessage());
        }
        return airplanes;
    }

    @Override
    public void update(Airplane obj) {
        String query = "UPDATE airplanes SET model_id = ?, pressurization_cycles = ?, last_revision_cycles = ?, " +
                "airplane_status = ? WHERE tail_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, obj.getAirplaneModel().getId());
            statement.setInt(2, obj.getPressurizationCycles());
            statement.setInt(3, obj.getLastRevisionCycles());
            statement.setString(4, obj.getAirplaneStatus().name());
            statement.setString(5, obj.getId());
            statement.executeUpdate();

            auditService.logUpdate("Airplane", obj.getId());
        } catch (SQLException e) {
            auditService.logError("AirplaneRepository", e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        String query = "DELETE FROM airplanes WHERE tail_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);
            statement.executeUpdate();
            auditService.logDelete("Airplane", id);
        } catch (SQLException e) {
            auditService.logError("AirplaneRepository", e.getMessage());
        }
    }
}
