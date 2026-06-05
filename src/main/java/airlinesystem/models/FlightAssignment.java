package airlinesystem.models;

public class FlightAssignment {
    private Employee employee;
    private Flight flight;
    private FlightRole role;

    public FlightAssignment(Employee employee, Flight flight, FlightRole role) {
        this.employee = employee;
        this.flight = flight;
        this.role = role;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Flight getFlight() {
        return flight;
    }

    public FlightRole getRole() {
        return role;
    }

    @Override
    public String toString() {
        return String.format(
                """
                FlightAssignment {
                employee ID: %s  |  name: %s %s
                flight: %s  |  role: %s
                }""",
                employee.getEmployeeId(), employee.getFirstName(), employee.getLastName(), flight.getFlightId(), role
        );
    }
}
