package airlinesystem.models;

import java.time.LocalDate;
import java.util.Set;

public abstract class Employee extends Person {
    private String employeeId;
    private LocalDate hireDate;
    private double salary;

    public Employee(int personId, String firstName, String lastName, LocalDate dateOfBirth, Set<Country> nationalities,
                    String email, String phoneNumber, Set<IdentityDocument> documents,
                    String employeeId, LocalDate hireDate, double salary)
    {
        super(personId, firstName, lastName, dateOfBirth, nationalities, email, phoneNumber, documents);
        this.employeeId = employeeId;
        this.hireDate = hireDate;
        this.salary = salary;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public double getSalary() {
        return salary;
    }
}