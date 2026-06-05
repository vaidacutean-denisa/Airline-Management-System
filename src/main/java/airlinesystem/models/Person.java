package airlinesystem.models;

import java.time.LocalDate;
import java.time.Period;
import java.util.Set;
import java.util.HashSet;

public abstract class Person {
    private int personId;                       // to uniquely identify a person in the DB (abstract of realistic IDs such as CNP, SSN etc)
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Set<Country> nationalities = new HashSet<>();

    private String email;
    private String phoneNumber;

    private Set<IdentityDocument> documents = new HashSet<>();                   // visa, passport, national_id

    public Person(int personId, String firstName, String lastName, LocalDate dateOfBirth, Set<Country> nationalities,
                  String email, String phoneNumber, Set<IdentityDocument> documents) {
        if (lastName == null || lastName.isBlank() || firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First and last name cannot be null or blank");
        }

        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.nationalities = nationalities;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.documents = documents;
    }

    public int getPersonId() {
        return personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Set<Country> getNationalities() {
        return new HashSet<>(nationalities);
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Set<IdentityDocument> getDocuments() {
        return new HashSet<>(documents);
    }

    public int getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    @Override
    public String toString() {
        return String.format(
                """
                    person ID: %d  |  first name: %s  |  last name: %s  |  date of birth: %s
                    nationalities: %s
                    email: %s  |  phone number: %s  |  documents: %s
                """,
                personId, firstName, lastName, dateOfBirth, nationalities, email, phoneNumber, documents
        );
    }
}