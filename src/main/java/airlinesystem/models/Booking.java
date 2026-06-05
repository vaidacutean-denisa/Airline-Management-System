package airlinesystem.models;

public class Booking implements Comparable<Booking> {
    private String ticketId;
    private Flight flight;
    private Passenger passenger;
    private CabinClasses cabinClass;                        // must validate the selected cabin class for the current flight (some airplane models might have only economy/ business seats)
    private double luggageWeight;

    public Booking(String ticketId, Flight flight, Passenger passenger, CabinClasses cabinClass, double luggageWeight) {
        if (luggageWeight < 0 || luggageWeight > 32.0) {
            throw new IllegalArgumentException("Luggage weight must be between 0 and 32 kg");
        }
        this.ticketId = ticketId;
        this.flight = flight;
        this.passenger = passenger;
        this.cabinClass = cabinClass;
        this.luggageWeight = luggageWeight;
    }

    @Override
    public int compareTo(Booking other) {                                   // to be able to automatically sort bookings by ticket ID in a data structure
        if (other == null) return 1;
        return this.ticketId.compareTo(other.getTicketId());
    }

    public String getTicketId() {
        return ticketId;
    }

    public Flight getFlight() {
        return flight;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public CabinClasses getCabinClass() {
        return cabinClass;
    }

    public double getLuggageWeight() {
        return luggageWeight;
    }

    @Override
    public String toString() {
        return String.format(
                """
                Booking {
                    ticket ID: %s  |  flight: %s  |  passenger: %s %s, ID: %d
                    cabin class: %s
                    luggage weight: %.2f
                }""",
                ticketId, flight != null ? flight.getFlightId() : "N/A",
                passenger.getFirstName(), passenger.getLastName(), passenger.getPersonId(), cabinClass, luggageWeight
        );
    }
}
