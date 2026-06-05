package airlinesystem.services;

import airlinesystem.models.Booking;
import airlinesystem.models.Passenger;
import airlinesystem.models.Flight;
import airlinesystem.models.CabinClasses;
import airlinesystem.models.FlightStatus;
import airlinesystem.repository.BookingRepository;

import java.util.List;
import java.util.Map;

public class BookingService {
    private final BookingRepository bookingRepository;
    private final FlightService flightService;
    private final PassengerService passengerService;

    public BookingService(BookingRepository bookingRepository, FlightService flightService, PassengerService passengerService) {
        this.bookingRepository = bookingRepository;
        this.flightService = flightService;
        this.passengerService = passengerService;
    }

    private void validateBooking(Booking booking) {
        if (booking == null || booking.getPassenger() == null || booking.getFlight() == null) {
            throw new IllegalArgumentException("Booking cannot be null and must have valid passenger and flight information");
        }

        int passengerId = booking.getPassenger().getPersonId();
        Passenger passenger = passengerService.getPassenger(passengerId);
        if (passenger == null) {
            throw new IllegalArgumentException("The specified passenger does not exist in the database.");
        }

        String flightId = booking.getFlight().getFlightId();
        Flight flight = flightService.getFlight(flightId);
        if (flight == null) {
            throw new IllegalArgumentException("The specified flight does not exist in the database.");
        }

        passengerService.verifyDocuments(passenger, flight);
        validateCabinCapacity(flight, booking.getCabinClass());
    }

    private void validateCabinCapacity(Flight flight, CabinClasses cabinClass) {
        Map<CabinClasses, Integer> cabinCapacity = flight.getAirplane().getAirplaneModel().getCabinCapacity();
        int maxSeatsPerClass = cabinCapacity.getOrDefault(cabinClass, 0);

        if (maxSeatsPerClass == 0) {
            throw new IllegalArgumentException("The flight's aircraft model does not have the specified class.");
        }

        int occupiedSeats = getOccupiedSeats(flight, cabinClass);
        if (occupiedSeats >= maxSeatsPerClass) {
            throw new IllegalArgumentException("The specified cabin class is fully booked.");
        }
    }

    private int getOccupiedSeats(Flight flight, CabinClasses cabinClass) {
        List<Booking> bookings = bookingRepository.getAll();
        int occupiedSeats = 0;
        for (Booking booking : bookings) {
            String bookingFlightId = booking.getFlight().getFlightId();
            String flightId = flight.getFlightId();

            if (bookingFlightId.equals(flightId) && booking.getCabinClass() == cabinClass) {
                occupiedSeats++;
            }
        }
        return occupiedSeats;
    }

    private void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Booking ID cannot be null or blank");
        }
    }

    public void addBooking(Booking booking) {
        validateBooking(booking);

        String flightId = booking.getFlight().getFlightId();
        Flight flight = flightService.getFlight(flightId);

        if (flight.getStatus() != FlightStatus.SCHEDULED) {
            throw new IllegalStateException("Cannot book passengers for a flight that is not scheduled.");
        }

        bookingRepository.add(booking);
        flight.addBooking(booking);
        flightService.updateFlight(flight);
    }

    public Booking getBooking(String id) {
        validateId(id);
        return bookingRepository.get(id);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.getAll();
    }

    public void updateBooking(Booking booking) {
        validateBooking(booking);
        bookingRepository.update(booking);
    }

    public void deleteBooking(String id) {
        validateId(id);
        bookingRepository.delete(id);
    }
}
