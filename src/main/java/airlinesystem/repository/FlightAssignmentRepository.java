package airlinesystem.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import airlinesystem.models.FlightAssignment;
import airlinesystem.models.FlightRole;
import airlinesystem.models.Flight;
import airlinesystem.models.Employee;
import airlinesystem.services.AuditService;

public class FlightAssignmentRepository implements GenericRepository<FlightAssignment> {
    private final Connection connection;
    private final AuditService auditService;
    private final FlightRepository flightRepository;

    // employees that can be assigned to a flight
    private final PilotRepository pilotRepository;
    private final FlightAttendantRepository flightAttendantRepository;

    public FlightAssignmentRepository(Connection connection, AuditService auditService,
                                      FlightRepository flightRepository, PilotRepository pilotRepository,
                                      FlightAttendantRepository flightAttendantRepository) {
        this.connection = connection;
        this.auditService = auditService;
        this.flightRepository = flightRepository;
        this.pilotRepository = pilotRepository;
        this.flightAttendantRepository = flightAttendantRepository;
    }

    @Override
    public void add(FlightAssignment obj) {
        String query = "INSERT INTO flight_assignments (employee_person_id, flight_id, assignment_role) VALUES (?, ?, ?)";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            int employeeId = 0;
            if (obj.getEmployee() != null) {
                employeeId = obj.getEmployee().getPersonId();
            }

            String flightId = null;
            if (obj.getFlight() != null) {
                flightId = obj.getFlight().getFlightId();
            }

            String roleName = null;
            if (obj.getRole() != null) {
                roleName = obj.getRole().name();
            }

            st.setInt(1, employeeId);
            st.setString(2, flightId);
            st.setString(3, roleName);

            st.executeUpdate();
            auditService.logAdd("FlightAssignment", employeeId + "-" + flightId);
        } catch (SQLException e) {
            auditService.logError("FlightAssignmentRepositoryAdd", e.getMessage());
        }
    }

    @Override
    public FlightAssignment get(String id) {
        // the generic method accepts a single id; a flight_assignment has a composite key, so we must split it into two (must respect the specific format, though: (employeeId, flightId))
        String[] parts = id.split(",");
        if (parts.length != 2) {
            auditService.logError("FlightAssignmentRepositoryGet", "Invalid composite ID format. Expected 'employeeId,flightId'");
            return null;
        }

        int employeeId = Integer.parseInt(parts[0].trim());
        String flightId = parts[1].trim();

        String query = "SELECT assignment_role FROM flight_assignments WHERE employee_person_id = ? AND flight_id = ?";
        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setInt(1, employeeId);
            st.setString(2, flightId);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    FlightRole role = FlightRole.valueOf(rs.getString("assignment_role"));
                    Flight flight = flightRepository.get(flightId);

                    // must check whether the employee is pilot or flight attendant
                    Employee employee = pilotRepository.get(String.valueOf(employeeId));
                    if (employee == null) {
                        employee = flightAttendantRepository.get(String.valueOf(employeeId));
                    }

                    auditService.logGet("FlightAssignment", id);
                    return new FlightAssignment(employee, flight, role);
                }
            }
        } catch (SQLException e) {
            auditService.logError("FlightAssignmentRepositoryGet", e.getMessage());
        }
        return null;
    }

    @Override
    public List<FlightAssignment> getAll() {
        List<FlightAssignment> assignments = new ArrayList<>();
        String query = "SELECT employee_person_id, flight_id FROM flight_assignments";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int empId = resultSet.getInt("employee_person_id");
                String flId = resultSet.getString("flight_id");

                // must concatenate the two parts of the composite key so that we could call the get method
                FlightAssignment assignment = get(empId + "," + flId);
                if (assignment != null) {
                    assignments.add(assignment);
                }
            }
            auditService.logGet("All flight assignments", "");
        } catch (SQLException e) {
            auditService.logError("FlightAssignmentRepositoryGetAll", e.getMessage());
        }
        return assignments;
    }

    @Override
    public void update(FlightAssignment obj) {
        String query = "UPDATE flight_assignments SET assignment_role = ? WHERE employee_person_id = ? AND flight_id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            int employeeId = 0;
            if (obj.getEmployee() != null) {
                employeeId = obj.getEmployee().getPersonId();
            }

            String flightId = null;
            if (obj.getFlight() != null) {
                flightId = obj.getFlight().getFlightId();
            }

            String roleName = null;
            if (obj.getRole() != null) {
                roleName = obj.getRole().name();
            }
            st.setString(1, roleName);
            st.setInt(2, employeeId);
            st.setString(3, flightId);

            st.executeUpdate();
            auditService.logUpdate("FlightAssignment", employeeId + "-" + flightId);
        } catch (SQLException e) {
            auditService.logError("FlightAssignmentRepositoryUpdate", e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        String[] parts = id.split(",");
        if (parts.length != 2) {
            auditService.logError("FlightAssignmentRepository", "Invalid composite ID format. Expected 'employeeId,flightId'");
            return;
        }

        int employeeId = Integer.parseInt(parts[0].trim());
        String flightId = parts[1].trim();

        String query = "DELETE FROM flight_assignments WHERE employee_person_id = ? AND flight_id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setInt(1, employeeId);
            st.setString(2, flightId);

            st.executeUpdate();
            auditService.logDelete("FlightAssignment", id);
        } catch (SQLException e) {
            auditService.logError("FlightAssignmentRepositoryDelete", e.getMessage());
        }
    }
}