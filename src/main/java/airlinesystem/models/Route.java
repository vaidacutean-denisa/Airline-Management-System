package airlinesystem.models;

import java.time.Duration;

public class Route {
    private String id;
    private Airport departureAirport;
    private Airport arrivalAirport;
    private double distanceKm;
    private Duration estimatedDuration;

    public Route(String id, Airport departureAirport, Airport arrivalAirport, double distanceKm, Duration estimatedDuration) {
        if (distanceKm <= 0 || estimatedDuration.isNegative()) {
            throw new IllegalArgumentException("Route distance and duration must be positive");
        }

        this.id = id;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.distanceKm = distanceKm;
        this.estimatedDuration = estimatedDuration;
    }

    public String getId() {
        return id;
    }

    public Airport getDepartureAirport() {
        return departureAirport;
    }

    public Airport getArrivalAirport() {
        return arrivalAirport;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public Duration getEstimatedDuration() {
        return estimatedDuration;
    }

    @Override
    public String toString() {
        return String.format(
                """
                Route {
                id: %s  |  departure airport: %s  |  arrival airport: %s
                distance (km): %.2f  |  estimated duration: %s
                }""",
                id, departureAirport.getName(), arrivalAirport.getName(), distanceKm, estimatedDuration
        );
    }

}