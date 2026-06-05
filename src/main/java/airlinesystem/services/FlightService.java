package airlinesystem.services;

import airlinesystem.models.Flight;
import airlinesystem.models.Passenger;
import airlinesystem.models.Airplane;
import airlinesystem.models.AirplaneModel;
import airlinesystem.models.Route;
import airlinesystem.models.FlightStatus;
import airlinesystem.models.Booking;
import airlinesystem.repository.FlightRepository;
import airlinesystem.services.RouteService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class FlightService {
    private final FlightRepository flightRepository;
    private final RouteService routeService;
    private final AirplaneService airplaneService;
    private final AirplaneModelService airplaneModelService;

    public FlightService(FlightRepository flightRepository, RouteService routeService, AirplaneService airplaneService, AirplaneModelService airplaneModelService) {
        this.flightRepository = flightRepository;
        this.routeService = routeService;
        this.airplaneService = airplaneService;
        this.airplaneModelService = airplaneModelService;
    }

    public void validateFlight(Flight flight) {
        if (flight == null || flight.getRoute() == null || flight.getAirplane() == null) {
            throw new IllegalArgumentException("Flight, route, and airplane cannot be null.");
        }
        String routeId = flight.getRoute().getId();
        Airplane airplane = flight.getAirplane();

        if (routeService.getRoute(routeId) == null) {
            throw new IllegalArgumentException("The specified route does not exist in the database.");
        }
        if (airplaneService.getAirplane(airplane.getId()) == null) {
            throw new IllegalArgumentException("The specified airplane does not exist in the database.");
        }

        AirplaneModel model = airplane.getAirplaneModel();
        double routeDistance = flight.getRoute().getDistanceKm();
        if (routeDistance > model.getMaxRange()) {
            throw new IllegalArgumentException("The specified route is too long for the airplane's range.");
        }

        double fuelNeeded = airplaneModelService.calculateFuelNeeded(model.getId(), routeDistance);
        if (fuelNeeded > model.getFuelCapacity()) {
            throw new IllegalArgumentException("The specified route requires more fuel than the airplane's capacity.");
        }
    }

    private void validateId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Flight ID cannot be null or empty.");
        }
    }

    public List<Airplane> getValidAirplanes(String routeId) {
        Route route = routeService.getRoute(routeId);
        double routeDistance = route.getDistanceKm();

        List<AirplaneModel> validModels = airplaneModelService.getModelsWithMinRange(routeDistance);
        List<Airplane> airplanes = airplaneService.getAllAirplanes();

        return airplanes.stream().filter(airplane -> validModels.contains(airplane.getAirplaneModel()))
                .collect(Collectors.toList());
    }

    public void addFlight(Flight flight) {
        validateFlight(flight);
        flightRepository.add(flight);
    }

    public Flight getFlight(String id) {
        validateId(id);
        return flightRepository.get(id);
    }

    public List<Flight> getAllFlights() {
        return flightRepository.getAll();
    }

    public void updateFlight(Flight flight) {
        validateFlight(flight);
        flightRepository.update(flight);
    }

    public void deleteFlight(String id) {
        validateId(id);
        flightRepository.delete(id);
    }

    // note: a better approach would be using a background scheduler to change statuses; but even this method would need user interaction, since in real-life the ATC orchestrates departures; the following approach might be more pragmatic
    public void departFlight(String id) {
        validateId(id);
        Flight flight = flightRepository.get(id);
        if (flight == null) {
            throw new IllegalArgumentException("The specified flight does not exist in the database.");
        }

        if (flight.getDepartureTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot depart a flight that is scheduled for the future.");
        }

        if (flight.getStatus() != FlightStatus.SCHEDULED) {
            throw new IllegalStateException("Flight status must be SCHEDULED to authorize departure.");
        }

        flight.setStatus(FlightStatus.IN_PROGRESS);
        flightRepository.update(flight);                                                // note: in future versions might check and update airplane status as well (since one option "grounded" is for safety risks)
    }

    public void confirmFlightArrival(String id) {
        validateId(id);
        Flight flight = flightRepository.get(id);
        if (flight == null) {
            throw new IllegalArgumentException("The specified flight does not exist in the database.");
        }
        if (flight.getStatus() != FlightStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot mark flight as arrived if it is not in progress.");
        }
        flight.setStatus(FlightStatus.COMPLETED);
        flightRepository.update(flight);

        Airplane airplane = flight.getAirplane();
        this.airplaneService.recordFlight(airplane);
    }

    public void cancelFlight(String id) {
        validateId(id);
        Flight flight = flightRepository.get(id);
        if (flight == null) {
            throw new IllegalArgumentException("The specified flight does not exist in the database.");
        }
        if (flight.getStatus() == FlightStatus.CANCELLED) {
            throw new IllegalStateException("The specified flight is already cancelled.");
        }

        flight.setStatus(FlightStatus.CANCELLED);
        flightRepository.update(flight);
    }

    public int getAvailableSeats(String id) {
        validateId(id);
        Flight flight = flightRepository.get(id);
        if (flight == null) {
            throw new IllegalArgumentException("The specified flight does not exist in the database.");
        }

        int currentBookings = flight.getBookings().size();
        int maxCapacity = flight.getAirplane().getAirplaneModel().getSeats();

        return maxCapacity - currentBookings;
    }

    public List<Passenger> getPassengerList(String id) {
        validateId(id);
        Flight flight = flightRepository.get(id);
        if (flight == null) {
            throw new IllegalArgumentException("The specified flight does not exist in the database.");
        }

        return flight.getBookings().stream().map(Booking::getPassenger)                     // gets the passenger of every booking; the final result will be a list of passengers
                .collect(Collectors.toList());
    }
}
