package airlinesystem.models;

import java.time.LocalDate;
import java.util.Set;

public class Passenger extends Person {
//    private int flyerPoints; the passenger may use these points to get vouchers/upgrades (the idea might be useful)
    private Boolean needsAssistance;

    public Passenger(int personId, String firstName, String lastName, LocalDate dateOfBirth, Set<Country> nationalities,
                     String email, String phoneNumber, Set<IdentityDocument> documents, Boolean needsAssistance)
    {
        super(personId, firstName, lastName, dateOfBirth, nationalities, email, phoneNumber, documents);

        this.needsAssistance = needsAssistance;
    }

    public Boolean getNeedsAssistance() {
        return needsAssistance;
    }

    @Override
    public String toString() {
        return "Passenger {" + super.toString()
                + "  |  needs assistance: " + needsAssistance + "}";
    }
}