package airlinesystem.utils;

import airlinesystem.models.Country;
import airlinesystem.models.DocumentType;
import airlinesystem.models.IdentityDocument;
import airlinesystem.services.CountryService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static airlinesystem.utils.ReaderUtils.*;

public class PersonReaderUtils {
    private final CountryService countryService;

    public PersonReaderUtils(CountryService countryService) {
        this.countryService = countryService;
    }

    // in this approach, the nationality of a person is the country object itself (to simplify logic)
    public Set<Country> readNationalities() {
        if (countryService.getAllCountries().isEmpty()) {
            System.out.println("There are no countries available to select nationality. Try adding one first.");
            return null;
        }
        System.out.println("Enter passenger nationality: (at least one required; press Enter when done or type 'exit' to cancel) ");
        Set<Country> nationalities = new HashSet<>();

        while (true) {
            String inputIso = readOptionalString("- Country ISO code: ");
            if ("exit".equalsIgnoreCase(inputIso.trim())) {
                System.out.println("Operation cancelled.");
                return null;
            }

            if (inputIso.isBlank()) {
                if (nationalities.isEmpty()) {
                    System.out.println("A passenger must have at least one nationality.");
                    continue;
                }
                break;
            }
            Country country = countryService.getCountry(inputIso);
            if (country == null) {
                System.out.println("The specified country does not exist in the database. Please retry.");
                continue;
            }
            if (nationalities.contains(country)) {
                System.out.println("The specified nationality was already added.");
                continue;
            }
            nationalities.add(country);
        }
        return nationalities;
    }

    // consider refactoring
    public Set<IdentityDocument> readIdentityDocuments() {
        if (countryService.getAllCountries().isEmpty()) {
            System.out.println("There are no countries available to issue identity documents. Try adding one first.");
            return null;
        }

        System.out.println("Enter passenger identity documents: (at least one required; press Enter when done or type 'exit' to cancel)");
        Set<IdentityDocument> documents = new HashSet<>();

        while (true) {
            String docNumber = readOptionalString("- Document number (ID): ");
            if ("exit".equalsIgnoreCase(docNumber.trim())) {
                System.out.println("Operation cancelled.");
                return null;
            }
            if (docNumber.isBlank()) {
                if (documents.isEmpty()) {
                    System.out.println("A passenger must have at least one identity document.");
                    continue;
                }
                break;
            }
            printDocumentTypes();
            DocumentType docType;
            while (true) {
                try {
                    String typeInput = readString("Enter document type: ").toUpperCase();
                    docType = DocumentType.valueOf(typeInput);
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid document type. Please choose from the listed options.");
                }
            }

            LocalDate expiryDate = readDate("Enter expiry date (dd/MM/yyyy): ");
            Country issuingCountry;
            while (true) {
                String countryIso = readString("Enter issuing country ISO code: ");
                issuingCountry = countryService.getCountry(countryIso);
                if (issuingCountry != null) {
                    break;
                }
                System.out.println("The specified country does not exist in the database. Please retry.");
            }

            IdentityDocument doc = new IdentityDocument(docNumber, docType, expiryDate, issuingCountry);
            documents.add(doc);
            System.out.println("Document added successfully. ");
        }

        return documents;
    }


    private void printDocumentTypes() {
        System.out.print("Available document types: ");
        for (DocumentType type : DocumentType.values()) {
            System.out.print(type + " ");
        }
        System.out.println();
    }

}
