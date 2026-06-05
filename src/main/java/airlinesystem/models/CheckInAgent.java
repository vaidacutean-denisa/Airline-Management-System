package airlinesystem.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CheckInAgent extends Employee {
    private List<String> languagesSpoken;
    private Airport assignedAirport;

    public CheckInAgent(int personId, String firstName, String lastName, LocalDate dateOfBirth, Set<Country> nationalities,
                           String email, String phoneNumber, Set<IdentityDocument> documents,
                           String employeeId, LocalDate hireDate, double salary,
                           List<String> languagesSpoken, Airport assignedAirport) {
        super(personId, firstName, lastName, dateOfBirth, nationalities, email, phoneNumber, documents,
                employeeId, hireDate, salary);

        this.languagesSpoken = new ArrayList<>(languagesSpoken);
        this.assignedAirport = assignedAirport;
    }

    public List<String> getLanguagesSpoken() {
        return new ArrayList<>(languagesSpoken);
    }

    public Airport getAssignedAirport() { return assignedAirport; }

    @Override
    public String toString() {
        return String.format(
                """
                Check-in agent {
                    ID: %d  |  Name: %s %s
                    languages spoken: %s
                    assigned airport: %s
                    identity documents: %s
                }""",
                getPersonId(), getFirstName(), getLastName(), String.join(", ", languagesSpoken), assignedAirport.getName(), getDocuments()
        );
    }
}
