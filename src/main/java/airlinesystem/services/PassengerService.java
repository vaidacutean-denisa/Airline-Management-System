package airlinesystem.services;

import airlinesystem.models.Flight;
import airlinesystem.models.Passenger;
import airlinesystem.models.Country;
import airlinesystem.models.EconomicBlock;
import airlinesystem.models.DocumentType;
import airlinesystem.models.IdentityDocument;
import airlinesystem.repository.PassengerRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class PassengerService {
    private final PassengerRepository passengerRepository;
    private final RouteService routeService;

    public PassengerService(PassengerRepository passengerRepository, RouteService routeService) {
        this.passengerRepository = passengerRepository;
        this.routeService = routeService;
    }

    private void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Input ID cannot be null or blank");
        }
    }

    public void addPassenger(Passenger passenger) {
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null");
        }
        passengerRepository.add(passenger);
    }

    public Passenger getPassenger(int id) {
        validateId(String.valueOf(id));
        return passengerRepository.get(String.valueOf(id));
    }

    public List<Passenger> getAllPassengers() {
        return passengerRepository.getAll();
    }

    public void updatePassenger(Passenger passenger) {
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null");
        }
        passengerRepository.update(passenger);
    }

    public void deletePassenger(int id) {
        validateId(String.valueOf(id));
        passengerRepository.delete(String.valueOf(id));
    }

    // must check whether the flight is domestic / international / within the same economic block; each requires different types of id cards
    private boolean isWithinBlock(Flight flight) {
        Country origin = flight.getRoute().getDepartureAirport().getCity().getCountry();
        Country destination = flight.getRoute().getArrivalAirport().getCity().getCountry();

        EconomicBlock originBlock = origin.getEconomicBlock();
        EconomicBlock destBlock = destination.getEconomicBlock();

        if (originBlock == null || destBlock == null) {
            return false;
        }
        return originBlock.equals(destBlock);
    }

    // note: should refactor later
    public void verifyDocuments(Passenger passenger, Flight flight) {
        if (passenger == null || flight == null) {
            throw new IllegalArgumentException("Passenger and flight cannot be null");
        }
        LocalDate flightDate = flight.getDepartureTime().toLocalDate();
        List<IdentityDocument> validDocs = passenger.getDocuments().stream()                                    // must have at least one valid document
                .filter(doc -> doc.getExpiryDate().isAfter(flightDate)).toList();

        if (validDocs.isEmpty()) {
            throw new IllegalArgumentException("Passenger must have at least one valid document for the flight");
        }

        boolean sameBlock = isWithinBlock(flight);
        boolean isDomestic = !routeService.isInternational(flight.getRoute());

        // case 1: the passenger travels within the same economic block -> needs either a national id or a passport (also applies for domestic flights; note that the block might be null for some countries)
        if (sameBlock || isDomestic) {
            boolean hasDoc = validDocs.stream().anyMatch(doc -> doc.getDocumentType() == DocumentType.NATIONAL_ID
                                                                                || doc.getDocumentType() == DocumentType.PASSPORT);
            if (!hasDoc) {
                throw new IllegalArgumentException("Passenger must have a valid national ID or passport for domestic flights or flights within the same economic block.");
            }
            return;
        }

        // case 2: international flight, outside the same eco block -> must have a passport + visa
        boolean hasPassport = validDocs.stream().anyMatch(doc -> doc.getDocumentType() == DocumentType.PASSPORT);
        if (!hasPassport) {
            throw new IllegalArgumentException("Passenger must have a valid passport for international flights outside the same economic block.");
        }

        // note that the passenger might own citizenship of the destination country -> only passport needed
        Country destination = flight.getRoute().getArrivalAirport().getCity().getCountry();
        boolean isCitizen = passenger.getNationalities().stream()
                                      .anyMatch(nat -> nat.getId().equals(destination.getId()));

        // if not a citizen, must own a visa issued by the destination country
        if (!isCitizen) {
            boolean hasVisa = validDocs.stream().anyMatch(doc -> doc.getDocumentType() == DocumentType.VISA
                                                        && doc.getIssuingCountry() != null
                                                        && doc.getIssuingCountry().getId().equals(destination.getId()));

            if (!hasVisa) {
                throw new IllegalArgumentException("Passenger must have a valid visa issued by the destination country.");
            }
        }
    }

    public void updatePassengerFirstName(int personId, String firstName) {
        validateId(String.valueOf(personId));
        passengerRepository.updateFirstName(personId, firstName);
    }

    public void updatePassengerLastName(int personId, String lastName) {
        validateId(String.valueOf(personId));
        passengerRepository.updateLastName(personId, lastName);
    }

    public void updatePassengerPhoneNumber(int personId, String phoneNumber) {
        validateId(String.valueOf(personId));
        passengerRepository.updatePhoneNumber(personId, phoneNumber);
    }

    public void updatePassengerEmail(int personId, String email) {
        validateId(String.valueOf(personId));
        passengerRepository.updateEmail(personId, email);
    }

    public void updatePassengerAssistanceNeeds(int personId, boolean needsAssistance) {
        validateId(String.valueOf(personId));
        passengerRepository.updateAssistanceNeeds(personId, needsAssistance);
    }

    public void updatePassengerNationalities(int personId, Set<Country> nationalities) {
        validateId(String.valueOf(personId));
        passengerRepository.updateNationalities(personId, nationalities);
    }

    public void updatePassengerIdentityDocuments(int personId, Set<IdentityDocument> documents) {
        validateId(String.valueOf(personId));
        passengerRepository.updateIdentityDocuments(personId, documents);
    }
}
