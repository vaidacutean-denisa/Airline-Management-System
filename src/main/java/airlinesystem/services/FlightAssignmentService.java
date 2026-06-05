package airlinesystem.services;

import airlinesystem.models.FlightAssignment;
import airlinesystem.models.FlightAttendant;
import airlinesystem.models.Pilot;
import airlinesystem.models.Flight;
import airlinesystem.models.FlightRole;
import airlinesystem.repository.FlightAssignmentRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlightAssignmentService {
    private final FlightAssignmentRepository flightAssignmentRepository;
    private final FlightService flightService;
    private final FlightAttendantService flightAttendantService;
    private final PilotService pilotService;

    public FlightAssignmentService(FlightAssignmentRepository flightAssignmentRepository, FlightService flightService, FlightAttendantService flightAttendantService, PilotService pilotService) {
        this.flightAssignmentRepository = flightAssignmentRepository;
        this.flightService = flightService;
        this.flightAttendantService = flightAttendantService;
        this.pilotService = pilotService;
    }

    private void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Flight assignment ID cannot be null or blank");
        }
    }

    private void validateFlightAssignment(FlightAssignment flightAssignment) {
        if (flightAssignment == null) {
            throw new IllegalArgumentException("Flight assignment cannot be null");
        }
    }

    public void addFlightAssignment(FlightAssignment flightAssignment) {
        validateFlightAssignment(flightAssignment);
        flightAssignmentRepository.add(flightAssignment);
    }

    public FlightAssignment getFlightAssignment(String id) {
        validateId(id);
        return flightAssignmentRepository.get(id);
    }

    public List<FlightAssignment> getAllFlightAssignments() {
        return flightAssignmentRepository.getAll();
    }

    public void updateFlightAssignment(FlightAssignment flightAssignment) {
        validateFlightAssignment(flightAssignment);
        flightAssignmentRepository.update(flightAssignment);
    }

    public void deleteFlightAssignment(String id) {
        validateId(id);
        flightAssignmentRepository.delete(id);
    }

    public void assignFAtoFlight(String flightId, int FACount) {                        // FA stands for flight attendant
        if (FACount < 2 || FACount > 10) {
            throw new IllegalArgumentException("Invalid number of flight attendants. Must be between 2 and 10.");
        }
        Flight flight = flightService.getFlight(flightId);
        if (flight == null) {
            throw new IllegalArgumentException("The specified flight does not exist in the database.");
        }

        List<FlightAttendant> availableFAs = getAvailableFAs(flight);
        if (availableFAs.size() < FACount) {
            throw new IllegalStateException("Not enough available flight attendants for the specified flight.");
        }

        for (int i = 0; i < FACount; i++) {
            FlightAttendant fa = availableFAs.get(i);
            FlightAssignment assignment = new FlightAssignment(fa, flight, FlightRole.FLIGHT_ATTENDANT);
            addFlightAssignment(assignment);
        }
    }

    public List<FlightAttendant> getAvailableFAs(Flight flight) {
        List<String> destinationLanguages = flight.getRoute().getArrivalAirport().getCity().getCountry().getOfficialLanguages();
        Set<FlightAttendant> qualifiedAttendants = new HashSet<>();

        List<FlightAttendant> attendants = null;
        for (String language : destinationLanguages) {
            attendants = flightAttendantService.getAttendantsByLanguage(language);
            qualifiedAttendants.addAll(attendants);
        }
        List<FlightAttendant> availableFAs = new ArrayList<>();

        LocalDateTime flightStart = flight.getDepartureTime();
        LocalDateTime flightEnd = flight.getArrivalTime();

        // must check if attendant is already assigned to another flight during the time of the current one
        for (FlightAttendant attendant : qualifiedAttendants) {
            if (!isEmployeeBusy(attendant.getEmployeeId(), flightStart, flightEnd)) {
                availableFAs.add(attendant);
            }
        }
        return availableFAs;
    }

    private boolean isEmployeeBusy(String employeeId, LocalDateTime flightStart, LocalDateTime flightEnd) {
        List<FlightAssignment> assignments = flightAssignmentRepository.getAll();

        for (FlightAssignment assignment : assignments) {
            String assignedEmployeeId = assignment.getEmployee().getEmployeeId();

            if (assignedEmployeeId.equals(employeeId)) {
                Flight otherFlight = assignment.getFlight();
                LocalDateTime otherFlightStart = otherFlight.getDepartureTime();
                LocalDateTime otherFlightEnd = otherFlight.getArrivalTime();

                // [a, b] intersects [c, d] if a < d and c < b
                if (flightStart.isBefore(otherFlightEnd) && otherFlightStart.isBefore(flightEnd)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void assignPilotToFlight(String pilotId, String flightId, FlightRole role) {
        if (role != FlightRole.CAPTAIN && role != FlightRole.FIRST_OFFICER) {
            throw new IllegalArgumentException("Invalid role for pilot assignment. Must be CAPTAIN or FIRST_OFFICER.");
        }
        Flight flight = flightService.getFlight(flightId);
        if (flight == null) {
            throw new IllegalArgumentException("The specified flight does not exist in the database.");
        }

        Pilot pilot = pilotService.getPilot(pilotId);
        if (pilot == null) {
            throw new IllegalArgumentException("The specified pilot does not exist.");
        }
        pilotService.checkLicense(pilot, flight);

        if (isEmployeeBusy(pilot.getEmployeeId(), flight.getDepartureTime(), flight.getArrivalTime())) {
            throw new IllegalStateException("Pilot is not available for the specified flight.");
        }

        if (isRoleAssigned(flightId, role)) {
            throw new IllegalStateException("The flight already has a " + role + " assigned.");
        }

        FlightAssignment assignment = new FlightAssignment(pilot, flight, role);
        addFlightAssignment(assignment);
    }


    // a flight must have only one captain and one first officer
    private boolean isRoleAssigned(String flightId, FlightRole role) {
        List<FlightAssignment> allAssignments = flightAssignmentRepository.getAll();

        for (FlightAssignment assignment : allAssignments) {
            if (assignment.getFlight().getFlightId().equals(flightId)) {
                if (assignment.getRole() == role) {
                    return true;
                }
            }
        }
        return false;
    }

    // returns a list of available pilots that may fly the specified aircraft (for view purpose)
    public List<Pilot> getAvailablePilots(String flightId) {
        Flight flight = flightService.getFlight(flightId);
        if (flight == null) {
            throw new IllegalArgumentException("The specified flight does not exist.");
        }

        List<Pilot> certifiedPilots = pilotService.getPilotsForFlight(flight);
        List<Pilot> availablePilots = new ArrayList<>();

        LocalDateTime flightStart = flight.getDepartureTime();
        LocalDateTime flightEnd = flight.getArrivalTime();

        for (Pilot pilot : certifiedPilots) {
            if (!isEmployeeBusy(pilot.getEmployeeId(), flightStart, flightEnd)) {
                availablePilots.add(pilot);
            }
        }
        return availablePilots;
    }
}



