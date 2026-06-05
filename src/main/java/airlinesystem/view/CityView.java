package airlinesystem.view;

import airlinesystem.exceptions.InvalidId;
import airlinesystem.exceptions.InvalidOption;
import airlinesystem.models.City;
import airlinesystem.models.Country;
import airlinesystem.services.CountryService;
import airlinesystem.services.CityService;
import airlinesystem.services.AirportService;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.List;

import static airlinesystem.utils.ReaderUtils.*;
import static airlinesystem.utils.ExitOperationUtil.isExit;

public class CityView {
    private final CityService cityService;
    private final CountryService countryService;
    private final AirportService airportService;

    public CityView(CityService cityService, CountryService countryService, AirportService airportService) {
        this.cityService = cityService;
        this.countryService = countryService;
        this.airportService = airportService;
    }

    public void run() {
        while (true) {
            showCityMenu();
            try {
                int option = readOption();
                int status = execCityOption(option);
                if (status == -1) {
                    break;
                }
            } catch (InvalidOption e) {
                System.out.println(e.getMessage());
            } catch (InvalidId invalidId) {
                System.out.println("Invalid id.");
            }
        }
    }

    private void showCityMenu() {
        System.out.println("\n-------- City Menu --------");
        System.out.println("1. Add city");
        System.out.println("2. Remove city");
        System.out.println("3. List cities");
        System.out.println("4. Check a specific city");
        System.out.println("9. Back");
    }

    private int execCityOption(int option) throws InvalidId {
        switch (option) {
            case 1:
                addCity();
                break;
            case 2:
                deleteCity();
                break;
            case 3:
                listCities();
                break;
            case 4:
                checkCity();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
        }
        return 0;
    }

    private void addCity() {
        System.out.println("Enter city details: ");
        Country country = readCountry();
        if (country == null) {
            return;
        }

        String id = readString("City id (type 'exit' to cancel): ");
        if (isExit(id)) {
            return;
        }

        String name = readString("City name: ");

        ZoneId zoneId = null;
        while (zoneId == null) {
            String zoneInput = readString("Enter zone name (e.g. Europe/Bucharest, America/New_York) or type 'exit' to cancel:");
            if (isExit(zoneInput)) {
                return;
            }
            try {
                zoneId = ZoneId.of(zoneInput);
            } catch (DateTimeException e) {
                System.out.println("Unknown timezone ID. Please try again (e.g., America/Los_Angeles).");
            }
        }

        try {
            City newCity = new City(id, name, country, zoneId);
            cityService.addCity(newCity);
            System.out.println("City added successfully.");

        } catch (IllegalArgumentException e) {
            System.out.println("Failed to add city: " + e.getMessage());
        }
    }

    private Country readCountry() {
        if (countryService.getAllCountries().isEmpty()) {
            System.out.println("No countries available. Please add a country first.");
            return null;
        }
        while (true) {
            String isoCode = readString("Enter country ISO code (type 'exit' to cancel): ");
            if (isExit(isoCode)) {
                return null;
            }
            Country country = countryService.getCountry(isoCode);
            if (country != null) {
                return country;
            }
            System.out.println("Invalid country code. Please retry.");
        }
    }

    private void deleteCity() throws InvalidId {
        String cityId = readString("Enter city id (type 'exit' to cancel): ");
        if (isExit(cityId)) {
            return;
        }
        City city = cityService.getCity(cityId);
        if (city == null) {
            System.out.println("City with id " + cityId + " not found.");
            return;
        }
        cityService.deleteCity(cityId);
        System.out.println("City deleted successfully.");
    }

    private void listCities() {
        List<City> cities = cityService.getAllCities();
        if (cities.isEmpty()) {
            System.out.println("The list is empty. Try adding a city.");
            return;
        }
        System.out.println("\n================ Registered cities ================");
        for (City city : cities) {
            System.out.println(city);
            System.out.println("-------------------------------------------------------");
        }
    }

    private void checkCity() throws InvalidId {
        String cityId = readString("Enter city id (type 'exit' to cancel): ");
        if (isExit(cityId)) {
            return;
        }
        City city = cityService.getCity(cityId);
        if (city == null) {
            System.out.println("City with id " + cityId + " not found.");
            return;
        }
        while (true) {
            showCityIdMenu(city.getId());
            try {
                int option = readOption();
                int status = execCityIdOptions(option, city);
                if (status == -1) {
                    return;
                }
            } catch (InvalidOption e) {
                System.out.println("Invalid option. Please retry.");
            }
        }
    }

    private void showCityIdMenu(String cityId) {
        System.out.println("City ID: " + cityId + " menu");
        System.out.println("1. Show city country");
        System.out.println("2. Show city information (detailed)");
        System.out.println("3. List city airports");
        System.out.println("9. Exit");
    }

    private int execCityIdOptions(int option, City city) {
        switch (option) {
            case 1:
                System.out.println(city.getCountry());
                break;
            case 2:
                System.out.println(city);
                break;
            case 3:
                System.out.println(airportService.getAirportsByCity(city.getId()));
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
                break;
        }
        return 0;
    }
}
