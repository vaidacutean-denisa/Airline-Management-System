package airlinesystem.services;

import airlinesystem.models.Country;
import airlinesystem.repository.CountryRepository;

import java.util.List;

public class CountryService {
    private final CountryRepository countryRepository;

    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public void addCountry(Country country) {
        if (country == null) {
            throw new IllegalArgumentException("Country cannot be null");
        }
        countryRepository.add(country);
    }

    public Country getCountry(String isoCode) {
        if (isoCode == null || isoCode.isBlank()) {
            throw new IllegalArgumentException("ISO code cannot be null or empty");
        }
        return countryRepository.get(isoCode.toUpperCase());
    }

    public List<Country> getAllCountries() {
        return countryRepository.getAll();
    }

    public void updateCountry(Country country) {
        if (country == null) {
            throw new IllegalArgumentException("Country cannot be null");
        }
        countryRepository.update(country);
    }

    public void deleteCountry(String isoCode) {
        if (isoCode == null || isoCode.isBlank()) {
            throw new IllegalArgumentException("Country ISO code cannot be null or empty");
        }
        countryRepository.delete(isoCode.toUpperCase());
    }

    public void addOfficialLanguage(String isoCode, String language) {
        if (isoCode == null || isoCode.isBlank()) {
            throw new IllegalArgumentException("ISO code cannot be null or empty");
        }
        if (language == null || language.isBlank()) {
            throw new IllegalArgumentException("Language cannot be null or empty");
        }

        Country country = countryRepository.get(isoCode.toUpperCase());
        if (country == null) {
            throw new IllegalArgumentException("Country code " + isoCode + " not found.");
        }

        countryRepository.addOfficialLanguage(isoCode.toUpperCase(), language);
    }

    public void removeOfficialLanguage(String isoCode, String language) {
        if (isoCode == null || isoCode.isBlank()) {
            throw new IllegalArgumentException("ISO code cannot be null or empty");
        }
        if (language == null || language.isBlank()) {
            throw new IllegalArgumentException("Language cannot be null or empty");
        }

        Country country = countryRepository.get(isoCode.toUpperCase());
        if (country == null) {
            throw new IllegalArgumentException("Country code " + isoCode + " not found.");
        }

        if (!country.getOfficialLanguages().contains(language)) {
            throw new IllegalArgumentException(language + " is not an official language of this country.");
        }
        countryRepository.removeOfficialLanguage(isoCode.toUpperCase(), language);
    }
}
