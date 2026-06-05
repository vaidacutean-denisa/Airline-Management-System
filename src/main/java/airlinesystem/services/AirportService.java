package airlinesystem.services;

import airlinesystem.models.Airport;
import airlinesystem.models.City;
import airlinesystem.repository.AirportRepository;
import airlinesystem.services.CityService;

import java.util.List;

public class AirportService {
    private final AirportRepository airportRepository;
    private final CityService cityService;

    public AirportService(AirportRepository airportRepository, CityService cityService) {
        this.airportRepository = airportRepository;
        this.cityService = cityService;
    }

    private void validateAirport(Airport airport) {
        if (airport == null || airport.getCity() == null) {
            throw new IllegalArgumentException("Airport or city cannot be null");
        }

        String cityId = airport.getCity().getId();
        if (cityService.getCity(cityId) == null) {
            throw new IllegalArgumentException("The specified city does not exist in the database.");
        }
    }

    private void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Input ID cannot be null or blank");
        }
    }

    public void addAirport(Airport airport) {
        validateAirport(airport);
        airportRepository.add(airport);
    }

    public Airport getAirport(String id) {
        validateId(id);
        return airportRepository.get(id.toUpperCase());
    }

    public List<Airport> getAllAirports() {
        return airportRepository.getAll();
    }

    public void updateAirport(Airport airport) {
        validateAirport(airport);
        airportRepository.update(airport);
    }

    public void deleteAirport(String id) {
        validateId(id);
        airportRepository.delete(id.toUpperCase());
    }

    public List<Airport> getAirportsByCity(String id) {
        validateId(id);
        if (cityService.getCity(id) == null) {
            throw new IllegalArgumentException("The specified city does not exist in the database.");
        }
        return airportRepository.getAirportsByCity(id);
    }
}
