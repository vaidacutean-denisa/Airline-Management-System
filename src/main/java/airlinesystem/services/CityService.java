package airlinesystem.services;

import airlinesystem.models.City;
import airlinesystem.repository.CityRepository;
import airlinesystem.repository.CountryRepository;
import java.util.List;

public class CityService {
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;

    public CityService(CityRepository cityRepository, CountryRepository countryRepository) {
        this.cityRepository = cityRepository;
        this.countryRepository = countryRepository;
    }

    private void validateCity(City city) {
        if (city == null || city.getCountry() == null) {
            throw new IllegalArgumentException("City and country cannot be null");
        }

        String countryId = city.getCountry().getId();
        if (countryRepository.get(countryId) == null) {
            throw new IllegalArgumentException("The specified country does not exist in the database.");
        }
    }

    public void addCity(City city) {
        validateCity(city);
        cityRepository.add(city);
    }

    public City getCity(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("City ID cannot be null or blank");
        }
        return cityRepository.get(id);
    }

    public List<City> getAllCities() {
        return cityRepository.getAll();
    }

    public void updateCity(City city) {
        validateCity(city);
        cityRepository.update(city);
    }

    public void deleteCity(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("City ID cannot be null or blank");
        }
        cityRepository.delete(id);
    }

    public List<City> getCitiesByCountry(String isoCode) {
        return cityRepository.getCitiesByCountry(isoCode);
    }
}
