package airlinesystem.view;

import airlinesystem.exceptions.InvalidId;
import airlinesystem.exceptions.InvalidOption;
import airlinesystem.models.Booking;
import airlinesystem.models.CabinClasses;
import airlinesystem.models.Flight;
import airlinesystem.models.Passenger;
import airlinesystem.services.BookingService;
import airlinesystem.services.FlightService;
import airlinesystem.services.PassengerService;

import java.util.List;

import static airlinesystem.utils.ReaderUtils.*;
import static airlinesystem.utils.ExitOperationUtil.isExit;

public class BookingView {
    private final BookingService bookingService;
    private final FlightService flightService;
    private final PassengerService passengerService;

    public BookingView(BookingService bookingService, FlightService flightService, PassengerService passengerService) {
        this.bookingService = bookingService;
        this.flightService = flightService;
        this.passengerService = passengerService;
    }

    public void run() {
        while (true) {
            showBookingMenu();
            try {
                int option = readOption();
                int status = execBookingOption(option);
                if (status == -1) {
                    break;
                }
            } catch (InvalidOption e) {
                System.out.println(e.getMessage());
            } catch (InvalidId invalidId) {
                System.out.println("Invalid ID.");
            }
        }
    }

    private void showBookingMenu() {
        System.out.println("\n-------- Booking Menu --------");
        System.out.println("1. Add booking");
        System.out.println("2. Remove booking");
        System.out.println("3. List bookings");
        System.out.println("4. Check a specific booking");
        System.out.println("9. Back");
    }

    private int execBookingOption(int option) throws InvalidId {
        switch (option) {
            case 1:
                addBooking();
                break;
            case 2:
                deleteBooking();
                break;
            case 3:
                listBookings();
                break;
            case 4:
                checkBooking();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
        }
        return 0;
    }

    private void addBooking() {
        System.out.println("Enter booking details (type 'exit' to cancel): ");
        String ticketId = readString("Ticket ID: ");
        if (isExit(ticketId)) {
            return;
        }
        Flight flight = readFlight();
        if (flight == null) {
            return;
        }
        Passenger passenger = readPassenger();
        if (passenger == null) {
            return;
        }
        CabinClasses cabinClass = readCabinClass();
        double luggageWeight = readDouble("Luggage weight: ");

        try {
            Booking booking = new Booking(ticketId, flight, passenger, cabinClass, luggageWeight);
            bookingService.addBooking(booking);
            System.out.println("Booking added successfully.");

        } catch (IllegalArgumentException e) {
            System.out.println("Error adding booking: " + e.getMessage());
        }
    }

    private Flight readFlight() {
        if (flightService.getAllFlights().isEmpty()) {
            System.out.println("No flights available. Try adding one first.");
            return null;
        }
        while (true) {
            String flightId = readString("Flight ID (type 'exit' to cancel): ");
            if (isExit(flightId)) {
                return null;
            }
            Flight flight = flightService.getFlight(flightId);
            if (flight != null) {
                int availableSeats = flightService.getAvailableSeats(flightId);
                System.out.println("Flight selected. Available seats remaining: " + availableSeats);
                return flight;
            }
            System.out.println("Invalid flight ID. Please retry.");
        }
    }

    private Passenger readPassenger() {
        if (passengerService.getAllPassengers().isEmpty()) {
            System.out.println("No passengers available. Try adding one first.");
            return null;
        }
        while (true) {
            int passengerId = readInt("Passenger ID (type 'exit' to cancel): ");
            Passenger passenger = passengerService.getPassenger(passengerId);
            if (passenger != null) {
                return passenger;
            }
            System.out.println("Invalid passenger ID. Please retry.");
        }
    }

    private CabinClasses readCabinClass() {
        while (true) {
            System.out.println("Available cabin classes:");
            for (CabinClasses cabinClass : CabinClasses.values()) {
                System.out.println("- " + cabinClass);
            }
            String input = readString("Cabin class: ").toUpperCase();
            if (isExit(input)) {
                return null;
            }
            try {
                return CabinClasses.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid cabin class. Please retry.");
            }
        }
    }

    private void deleteBooking() throws InvalidId {
        String ticketId = readString("Enter ticket ID: ");
        if (isExit(ticketId)) {
            return;
        }
        Booking booking = bookingService.getBooking(ticketId);
        if (booking == null) {
            System.out.println("Booking with ticket ID " + ticketId + " not found.");
            return;
        }
        bookingService.deleteBooking(ticketId);
        System.out.println("Booking deleted successfully.");
    }

    private void listBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        if (bookings.isEmpty()) {
            System.out.println("The list is empty. Try adding a booking.");
            return;
        }
        System.out.println("\n================ Registered bookings ================");
        for (Booking booking : bookings) {
            System.out.println(booking);
            System.out.println("-------------------------------------------------------");
        }
    }

    private void checkBooking() throws InvalidId {
        String ticketId = readString("Enter ticket ID (type 'exit' to cancel): ");
        if (isExit(ticketId)) {
            return;
        }
        Booking booking = bookingService.getBooking(ticketId);
        if (booking == null) {
            System.out.println("Booking with ticket ID " + ticketId + " not found.");
            return;
        }
        while (true) {
            showBookingIdMenu(ticketId);
            try {
                int option = readOption();
                int status = execBookingIdOption(option, booking);
                if (status == -1) {
                    return;
                }
            } catch (InvalidOption e) {
                System.out.println("Invalid option. Please retry.");
            }
        }
    }

    private void showBookingIdMenu(String ticketId) {
        System.out.println("Booking ticket ID: " + ticketId + " menu");
        System.out.println("1. Show booking details");
        System.out.println("2. Update booking");
        System.out.println("9. Exit");
    }

    private int execBookingIdOption(int option, Booking booking) throws InvalidId {
        switch (option) {
            case 1:
                printBooking(booking);
                break;
            case 2:
                updateBooking(booking);
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid option. Please retry.");
        }
        return 0;
    }

    private void updateBooking(Booking booking) throws InvalidId {
        String ticketId = booking.getTicketId();
        System.out.println("Enter updated booking details: ");

        Flight flight = readFlight();
        if (flight == null) {
            return;
        }
        Passenger passenger = readPassenger();
        if (passenger == null) {
            return;
        }
        CabinClasses cabinClass = readCabinClass();
        double luggageWeight = readDouble("Luggage weight: ");

        try {
            Booking updatedBooking = new Booking(ticketId, flight, passenger, cabinClass, luggageWeight);
            bookingService.updateBooking(updatedBooking);
            System.out.println("Booking updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating booking: " + e.getMessage());
        }
    }

    private void printBooking(Booking booking) {
        System.out.println("Ticket ID: " + booking.getTicketId());

        if (booking.getFlight() != null) {
            System.out.println("Flight ID: " + booking.getFlight().getFlightId());
        }
        if (booking.getPassenger() != null) {
            System.out.println("Passenger ID: " + booking.getPassenger().getPersonId());
            System.out.println("Passenger name: " + booking.getPassenger().getFirstName() + " " + booking.getPassenger().getLastName());
        }

        System.out.println("Cabin class: " + booking.getCabinClass());
        System.out.println("Luggage weight: " + booking.getLuggageWeight() + " kg");
    }
}