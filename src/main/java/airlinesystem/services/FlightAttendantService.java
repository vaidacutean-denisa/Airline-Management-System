package airlinesystem.services;

import airlinesystem.models.Flight;
import airlinesystem.models.FlightAttendant;
import airlinesystem.models.IdentityDocument;
import airlinesystem.repository.FlightAttendantRepository;
import airlinesystem.repository.FlightRepository;

import java.util.List;
import java.util.Set;

public class FlightAttendantService {
    private final FlightAttendantRepository flightAttendantRepository;

    public FlightAttendantService(FlightAttendantRepository flightAttendantRepository) {
        this.flightAttendantRepository = flightAttendantRepository;
    }

    private void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Flight attendant ID cannot be null or blank");
        }
    }

    public void addFlightAttendant(FlightAttendant flightAttendant) {
        if (flightAttendant == null) {
            throw new IllegalArgumentException("Flight attendant cannot be null");
        }
        flightAttendantRepository.add(flightAttendant);
    }

    public FlightAttendant getFlightAttendant(String id) {
        validateId(id);
        return flightAttendantRepository.get(id);
    }

    public List<FlightAttendant> getAllFlightAttendants() {
        return flightAttendantRepository.getAll();
    }

    // consider refactoring updates
    public void updateFlightAttendant(FlightAttendant flightAttendant) {
        if (flightAttendant == null) {
            throw new IllegalArgumentException("Flight attendant cannot be null");
        }
        flightAttendantRepository.update(flightAttendant);
    }

    public void updateFAFirstName(String attendantId, String firstName) {
        validateId(attendantId);
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank.");
        }
        if (flightAttendantRepository.get(attendantId) == null) {
            throw new IllegalArgumentException("Flight attendant not found.");
        }
        flightAttendantRepository.updateFirstName(attendantId, firstName);
    }

    public void updateFALastName(String attendantId, String lastName) {
        validateId(attendantId);
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank.");
        }
        if (flightAttendantRepository.get(attendantId) == null) {
            throw new IllegalArgumentException("Flight attendant not found.");
        }
        flightAttendantRepository.updateLastName(attendantId, lastName);
    }

    public void updateFAEmail(String attendantId, String email) {
        validateId(attendantId);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank.");
        }
        if (flightAttendantRepository.get(attendantId) == null) {
            throw new IllegalArgumentException("Flight attendant not found.");
        }
        flightAttendantRepository.updateEmail(attendantId, email);
    }

    public void updateFAIdDocs(String attendantId, Set<IdentityDocument> documents) {
        validateId(attendantId);
        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("Identity documents list cannot be null or empty.");
        }
        if (flightAttendantRepository.get(attendantId) == null) {
            throw new IllegalArgumentException("Flight attendant not found.");
        }
        flightAttendantRepository.updateIdentityDocuments(attendantId, documents);
    }

    public void deleteFlightAttendant(String id) {
        validateId(id);
        flightAttendantRepository.delete(id);
    }

    // the database filters flight attendants instead of getting all FAs in memory and filtering them by calling getLanguagesSpoken (better performance overall)
    public List<FlightAttendant> getAttendantsByLanguage(String language) {
        if (language == null || language.isBlank()) {
            throw new IllegalArgumentException("Language cannot be null or blank");
        }
        return flightAttendantRepository.getFlightAttendantsByLanguage(language);
    }
}
