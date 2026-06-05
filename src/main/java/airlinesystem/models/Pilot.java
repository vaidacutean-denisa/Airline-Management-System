package airlinesystem.models;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.time.LocalDate;

public class Pilot extends Employee {
    private String licenseNumber;
    private Map<AirplaneModel, LocalDate> certifications = new HashMap<>();

    public Pilot(int personId, String firstName, String lastName, LocalDate dateOfBirth, Set<Country> nationalities,
                 String email, String phoneNumber, Set<IdentityDocument> documents,
                 String employeeId, LocalDate hireDate, double salary,
                 String licenseNumber, Map<AirplaneModel, LocalDate> certifications)
    {
        super(personId, firstName, lastName, dateOfBirth, nationalities, email, phoneNumber, documents,
                employeeId, hireDate, salary);

        this.licenseNumber = licenseNumber;
        this.certifications = new HashMap<>(certifications);
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public Map<AirplaneModel, LocalDate> getCertifications() {
        return new HashMap<>(certifications);
    }

    public void addCertification(AirplaneModel airplaneModel, LocalDate expirationDate) {
        if (airplaneModel == null || expirationDate == null) {
            throw new IllegalArgumentException("Airplane model and expiration date cannot be null.");
        }
        certifications.put(airplaneModel, expirationDate);
    }

    @Override
    public String toString() {
        StringBuilder docs = new StringBuilder();

        // to print the name of the aircraft + the expiration date of the license properly
        if (certifications.isEmpty()) {
            docs.append("None");
        } else {
            certifications.forEach((model, expiryDate) ->
                    docs.append(String.format("\n        - %s (Expires: %s)", model.getModelName(), expiryDate))
            );
        }

        return String.format(
                """
                Pilot {
                    ID: %d  |  Name: %s %s
                    License: %s
                    Certifications: %s
                    Identity documents: %s
                }""",
                getPersonId(), getFirstName(), getLastName(), licenseNumber, docs.toString(), getDocuments()
        );
    }
}