package airlinesystem.models;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.time.LocalDate;

public class FlightAttendant extends Employee {
    private List<String> languagesSpoken;

    public FlightAttendant(int personId, String firstName, String lastName, LocalDate dateOfBirth, Set<Country> nationalities,
                           String email, String phoneNumber, Set<IdentityDocument> documents,
                           String employeeId, LocalDate hireDate, double salary,
                           List<String> languagesSpoken)
    {
        super(personId, firstName, lastName, dateOfBirth, nationalities, email, phoneNumber, documents,
                employeeId, hireDate, salary);

        this.languagesSpoken = new ArrayList<>(languagesSpoken);
    }

    public List<String> getLanguagesSpoken() {
        return new ArrayList<>(languagesSpoken);
    }

    @Override
    public String toString() {
        return String.format(
                """
                Flight attendant {
                    ID: %d  |  Name: %s %s
                    languages spoken: %s
                    identity documents: %s
                }""",
                getPersonId(), getFirstName(), getLastName(), String.join(", ", languagesSpoken), getDocuments()
        );
    }
}