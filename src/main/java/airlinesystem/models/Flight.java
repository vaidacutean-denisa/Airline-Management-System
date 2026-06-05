package airlinesystem.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.TreeSet;

public class Flight {
    private String flightId;
    private Route route;                                        // the departure & arrival airports can be deduced from the route
    private Airplane airplane;

    private LocalDateTime departureTime;
    private FlightStatus status;

    private Set<Booking> bookings = new TreeSet<>();

    public Flight(String flightId, Route route, Airplane airplane, LocalDateTime departureTime, FlightStatus flightStatus) {
        if (departureTime == null) {
            throw new IllegalArgumentException("Departure time cannot be null");
        }

        this.flightId = flightId;
        this.route = route;
        this.airplane = airplane;
        this.departureTime = departureTime;
        this.status = flightStatus;
    }

    public String getFlightId() {
        return flightId;
    }

    public Route getRoute() {
        return route;
    }

    public Airplane getAirplane() {
        return airplane;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    // note: see rich domain model vs anemic domain model
    public LocalDateTime getArrivalTime() {                                                         // keeping a separated variabile would violate the principle of single source of truth (which often leads to inconsistencies)
        ZoneId departureZone = this.route.getDepartureAirport().getCity().getTimezone();            // gets the timezone of each city
        ZoneId arrivalZone = this.route.getArrivalAirport().getCity().getTimezone();

        ZonedDateTime departureTime = ZonedDateTime.of(this.departureTime, departureZone);          // gets the local time of the departure city
        ZonedDateTime arrivalTimeDep = departureTime.plus(this.route.getEstimatedDuration());       // calculate the arrival time using the timezone of the departure city

        ZonedDateTime arrivalTime = arrivalTimeDep.withZoneSameInstant(arrivalZone);                // convert the local time to the timezone of the arrival city

        return arrivalTime.toLocalDateTime();
    }

    public FlightStatus getStatus() {
        return this.status;
    }

    public LocalDate getFlightDate() {
        return this.departureTime.toLocalDate();
    }

    public Set<Booking> getBookings() {
        return new TreeSet<>(bookings);
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    public void addBooking(Booking booking) {
        this.bookings.add(booking);
    }

    @Override
    public String toString() {
        return String.format(
                """
                Flight {
                id: %s  |  route: %s - %s
                departure time: %s  |  estimated arrival time: %s
                status: %s
                }"""
                , flightId, route.getDepartureAirport().getId(), route.getArrivalAirport().getId(),
                departureTime, getArrivalTime(), status
        );
    }
}
