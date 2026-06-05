package airlinesystem.view;

import airlinesystem.models.Country;
import airlinesystem.models.EconomicBlock;
import airlinesystem.services.CityService;
import airlinesystem.services.CountryService;
import static airlinesystem.utils.ReaderUtils.*;
import airlinesystem.exceptions.InvalidId;
import airlinesystem.exceptions.InvalidOption;

import java.util.ArrayList;
import java.util.List;
import static airlinesystem.utils.ExitOperationUtil.isExit;

public class CountryView {
    private final CountryService countryService;
    private final CityService cityService;

    public CountryView(CountryService countryService, CityService cityService) {
        this.countryService = countryService;
        this.cityService = cityService;
    }

    public void run() {
        while (true) {
            showCountryMenu();
            try {
                int option = readOption();
                int status = execCountryOption(option);
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

    private void showCountryMenu() {
        System.out.println("\n-------- Country Menu --------");
        System.out.println("1. Add country");
        System.out.println("2. Remove country");
        System.out.println("3. List countries");
        System.out.println("4. Check a specific country");
        System.out.println("9. Back");
    }

    private int execCountryOption(int option) throws InvalidId {
        switch (option) {
            case 1:
                addCountry();
                break;
            case 2:
                deleteCountry();
                break;
            case 3:
                listCountries();
                break;
            case 4:
                checkCountry();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
        }
        return 0;
    }

    private void addCountry() {
        System.out.println("Enter country details (or type 'exit' to cancel): ");
        String isoCode = readString("ISO code: ");
        if (isExit(isoCode)) {
            return;
        }
        String name = readString("Name: ");

        EconomicBlock economicBlock = readEconomicBlock();
        List<String> languages = readLanguages();

        try {
            Country country = new Country(isoCode, name, economicBlock, languages);
            countryService.addCountry(country);
            System.out.println("Country added successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Failed to add country: " + e.getMessage());
        }
    }

    private EconomicBlock readEconomicBlock() {
        String input = readOptionalString("Enter economic block: EU, MERCOSUR, ASEAN, AU, USMCA (or press Enter to skip)");
        if (input.isBlank())
            return null;

        try {
            return EconomicBlock.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid economic block. Please retry.");
            return readEconomicBlock();
        }
    }

    private List<String> readLanguages() {
        List<String> languages = new ArrayList<>();
        System.out.println("Enter official languages (press Enter to skip): ");
        while (true) {
            String language = readOptionalString("- Language: ");

            if (language.isEmpty()) {
                return languages;
            }
            if (!language.isBlank()) {
                if (!languages.contains(language)) {
                    languages.add(language);
                } else {
                    System.out.println("The specified language was already entered.");
                }
            }
        }
    }

    private void deleteCountry() {
        String isoCode = readString("Enter country ISO code (type 'exit' to cancel): ");
        if (isExit(isoCode)) {
            return;
        }
        Country country = countryService.getCountry(isoCode);

        if (country == null) {
            System.out.println("Country with ISO code: " + isoCode + " not found.");
            return;
        }
        countryService.deleteCountry(isoCode);
        System.out.println("Country deleted successfully.");
    }

    private void listCountries() {
        List<Country> countries = countryService.getAllCountries();
        if (countries.isEmpty()) {
            System.out.println("The list is empty. Try adding a country.");
            return;
        }
        System.out.println("\n================ Registered countries ================");
        for (Country country : countries) {
            System.out.println(country);
            System.out.println("-------------------------------------------------------");
        }
    }

    private void checkCountry() throws InvalidId {
        String isoCode = readString("Enter country ISO code (type 'exit' to cancel): ");
        if (isExit(isoCode)) {
            return;
        }
        Country country = countryService.getCountry(isoCode);
        if (country == null) {
            System.out.println("Country with ISO code " + isoCode + " not found.");
            return;
        }
        while (true) {
            // the user might update the object in memory -> we commit the changes in the DB so that, at the next read, we have the latest version
            country = countryService.getCountry(isoCode);
            showCountryIdMenu(country.getId());
            try {
                int option = readOption();
                int status = execCountryIdOptions(option, country);
                if (status == -1) {
                    return;
                }
            } catch (InvalidOption e) {
                System.out.println("Invalid option. Please retry.");
            }
        }
    }

    private void showCountryIdMenu(String isoCode) {
        System.out.println("Country ISO code " + isoCode + " menu");
        System.out.println("1. Show country information (detailed)");
        System.out.println("2. Add official language");
        System.out.println("3. Remove official language");
        System.out.println("4. List country cities");
        System.out.println("9. Exit");
    }

    private int execCountryIdOptions(int option, Country country) {
        switch (option) {
            case 1:
                System.out.println(country);
                break;
            case 2:
                addCountryLanguage(country);
                break;
            case 3:
                removeCountryLanguage(country);
                break;
            case 4:
                System.out.println(cityService.getCitiesByCountry(country.getId()));
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
        }
        return 0;
    }

    private void addCountryLanguage(Country country) {
        String language = readString("Enter a language to add (type 'exit' to cancel): ");
        if (isExit(language)) {
            return;
        }
        try {
            countryService.addOfficialLanguage(country.getId(), language);
            System.out.println("Language added successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error adding language: " + e.getMessage());
        }
    }

    private void removeCountryLanguage(Country country) {
        String language = readString("Enter a language to remove (type 'exit' to cancel): ");
        if (isExit(language)) {
            return;
        }
        try {
            countryService.removeOfficialLanguage(country.getId(), language);
            System.out.println("Language removed successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error removing language: " + e.getMessage());
        }
    }
}

