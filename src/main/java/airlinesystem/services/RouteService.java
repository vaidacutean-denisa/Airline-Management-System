package airlinesystem.services;

import airlinesystem.models.Route;
import airlinesystem.services.AirportService;
import airlinesystem.repository.RouteRepository;

import java.util.List;

public class RouteService {
    private final RouteRepository routeRepository;
    private final AirportService airportService;

    public RouteService(RouteRepository routeRepository, AirportService airportService) {
        this.routeRepository = routeRepository;
        this.airportService = airportService;
    }

    private void validateRoute(Route route) {
        if (route == null || route.getArrivalAirport() == null || route.getDepartureAirport() == null) {
            throw new IllegalArgumentException("Route cannot be null and must have valid departure and arrival airports");
        }

        String arrivalAirportId = route.getArrivalAirport().getId();
        String departureAirportId = route.getDepartureAirport().getId();

        if (airportService.getAirport(arrivalAirportId) == null) {
            throw new IllegalArgumentException("The specified arrival airport does not exist in the database.");
        }

        if (airportService.getAirport(departureAirportId) == null) {
            throw new IllegalArgumentException("The specified departure airport does not exist in the database.");
        }
    }

    public Route findRoute(String departureId, String arrivalId) {
        validateId(departureId);
        validateId(arrivalId);
        return routeRepository.findRoute(departureId.toUpperCase(), arrivalId.toUpperCase());
    }

    public void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Input ID cannot be null or blank");
        }
    }

    public void addRoute(Route route) {
        validateRoute(route);
        String departureId = route.getDepartureAirport().getId();
        String arrivalId = route.getArrivalAirport().getId();

        if (findRoute(departureId, arrivalId) != null) {
            throw new IllegalArgumentException("Route with the same departure and arrival airports already exists in the database.");
        }
        routeRepository.add(route);
    }

    public Route getRoute(String id) {
        validateId(id);
        return routeRepository.get(id);
    }

    public List<Route> getAllRoutes() {
        return routeRepository.getAll();
    }

    public void updateRoute(Route route) {
        validateRoute(route);
        routeRepository.update(route);
    }

    public void deleteRoute(String id) {
        validateId(id);
        routeRepository.delete(id);
    }

    public List<Route> getRoutesByAirport(String airport, RouteRepository.AirportRole role) {
        validateId(airport);
        return routeRepository.getRoutesByAirport(airport.toUpperCase(), role);
    }

    public boolean isInternational(Route route) {
        if (route == null) {
            throw new IllegalArgumentException("Route cannot be null");
        }
        // traverse the chain: route -> airport -> city -> country; must check if the flight is international or domestic, decided by whether countryArrival !=/== countryDeparture
        String departureCountry = route.getDepartureAirport().getCity().getCountry().getId();
        String arrivalCountry = route.getArrivalAirport().getCity().getCountry().getId();

        return !departureCountry.equals(arrivalCountry);
    }

    // note: could introduce a mechanism to create a route that includes layovers (ex: bucharest -> paris -> miami), but take that as a (possible) future feature
}
