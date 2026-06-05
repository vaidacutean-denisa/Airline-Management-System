package airlinesystem.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import airlinesystem.models.Booking;
import airlinesystem.models.CabinClasses;
import airlinesystem.models.Flight;
import airlinesystem.models.Passenger;
import airlinesystem.services.AuditService;

public class BookingRepository implements GenericRepository<Booking> {
    private final Connection connection;
    private final AuditService auditService;

    private final FlightRepository flightRepository;
    private final PassengerRepository passengerRepository;

    public BookingRepository(Connection connection, AuditService auditService,
                             FlightRepository flightRepository, PassengerRepository passengerRepository) {
        this.connection = connection;
        this.auditService = auditService;
        this.flightRepository = flightRepository;
        this.passengerRepository = passengerRepository;
    }

    @Override
    public void add(Booking obj) {
        String query = "INSERT INTO bookings (ticket_id, flight_id, passenger_person_id, cabin_class, luggage_weight) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, obj.getTicketId());

            String flightId = null;
            if (obj.getFlight() != null) {
                flightId = obj.getFlight().getFlightId();
            }

            int passengerId = 0;
            if (obj.getPassenger() != null) {
                passengerId = obj.getPassenger().getPersonId();
            }

            String cabinName = null;
            if (obj.getCabinClass() != null) {
                cabinName = obj.getCabinClass().name();
            }

            st.setString(2, flightId);
            st.setInt(3, passengerId);
            st.setString(4, cabinName);
            st.setDouble(5, obj.getLuggageWeight());

            st.executeUpdate();
            auditService.logAdd("Booking", obj.getTicketId());
        } catch (SQLException e) {
            auditService.logError("BookingRepositoryAdd", e.getMessage());
        }
    }

    @Override
    public Booking get(String id) {
        String query = "SELECT b.ticket_id, b.flight_id, b.passenger_person_id, b.cabin_class, b.luggage_weight " +
                "FROM bookings b " +
                "WHERE b.ticket_id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, id);

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    String ticketId = rs.getString("ticket_id");
                    String flightId = rs.getString("flight_id");
                    int passengerId = rs.getInt("passenger_person_id");
                    CabinClasses cabinClass = CabinClasses.valueOf(rs.getString("cabin_class"));
                    double luggageWeight = rs.getDouble("luggage_weight");

                    // FK to flight
                    Flight flight = flightRepository.get(flightId);
                    Passenger passenger = passengerRepository.get(String.valueOf(passengerId));

                    auditService.logGet("Booking", id);
                    return new Booking(ticketId, flight, passenger, cabinClass, luggageWeight);
                }
            }
        } catch (SQLException e) {
            auditService.logError("BookingRepositoryGet", e.getMessage());
        }
        return null;
    }

    @Override
    public List<Booking> getAll() {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT ticket_id FROM bookings";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Booking booking = get(resultSet.getString("ticket_id"));
                bookings.add(booking);
            }
            auditService.logGet("All bookings", "");
        } catch (SQLException e) {
            auditService.logError("BookingRepositoryGetAll", e.getMessage());
        }
        return bookings;
    }

    @Override
    public void update(Booking obj) {
        String query = "UPDATE bookings SET flight_id = ?, passenger_person_id = ?, cabin_class = ?, luggage_weight = ? WHERE ticket_id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, obj.getFlight() != null ? obj.getFlight().getFlightId() : null);
            st.setInt(2, obj.getPassenger() != null ? obj.getPassenger().getPersonId() : 0);
            st.setString(3, obj.getCabinClass() != null ? obj.getCabinClass().name() : null);
            st.setDouble(4, obj.getLuggageWeight());
            st.setString(5, obj.getTicketId());

            st.executeUpdate();
            auditService.logUpdate("Booking", obj.getTicketId());
        } catch (SQLException e) {
            auditService.logError("BookingRepositoryUpdate", e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        String query = "DELETE FROM bookings WHERE ticket_id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, id);
            st.executeUpdate();

            auditService.logDelete("Booking", id);
        } catch (SQLException e) {
            auditService.logError("BookingRepositoryDelete", e.getMessage());
        }
    }
}