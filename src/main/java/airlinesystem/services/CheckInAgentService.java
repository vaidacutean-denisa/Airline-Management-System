package airlinesystem.services;

import airlinesystem.models.CheckInAgent;
import airlinesystem.models.IdentityDocument;
import airlinesystem.repository.CheckInAgentRepository;

import java.util.List;
import java.util.Set;

public class CheckInAgentService {
    private final CheckInAgentRepository checkInAgentRepository;

    public CheckInAgentService(CheckInAgentRepository checkInAgentRepository) {
        this.checkInAgentRepository = checkInAgentRepository;
    }

    private void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Check-in agent ID cannot be null or blank");
        }
    }

    public void addCheckInAgent(CheckInAgent checkInAgent) {
        if (checkInAgent == null) {
            throw new IllegalArgumentException("Check-in agent cannot be null");
        }
        checkInAgentRepository.add(checkInAgent);
    }

    public CheckInAgent getCheckInAgent(String id) {
        validateId(id);
        return checkInAgentRepository.get(id);
    }

    public List<CheckInAgent> getAllCheckInAgents() {
        return checkInAgentRepository.getAll();
    }

    // consider refactoring updates
    public void updateCheckInAgent(CheckInAgent checkInAgent) {
        if (checkInAgent == null) {
            throw new IllegalArgumentException("Check-in agent cannot be null");
        }
        checkInAgentRepository.update(checkInAgent);
    }

    public void updateCheckInAgentFirstName(String agentId, String firstName) {
        validateId(agentId);
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank.");
        }
        if (checkInAgentRepository.get(agentId) == null) {
            throw new IllegalArgumentException("Check-in agent not found.");
        }
        checkInAgentRepository.updateFirstName(agentId, firstName);
    }

    public void updateCheckInAgentLastName(String agentId, String lastName) {
        validateId(agentId);
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank.");
        }
        if (checkInAgentRepository.get(agentId) == null) {
            throw new IllegalArgumentException("Check-in agent not found.");
        }
        checkInAgentRepository.updateLastName(agentId, lastName);
    }

    public void updateCheckInAgentEmail(String agentId, String email) {
        validateId(agentId);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank.");
        }
        if (checkInAgentRepository.get(agentId) == null) {
            throw new IllegalArgumentException("Check-in agent not found.");
        }
        checkInAgentRepository.updateEmail(agentId, email);
    }

    public void updateCheckInAgentIdentityDocuments(String agentId, Set<IdentityDocument> documents) {
        validateId(agentId);
        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("Identity documents list cannot be null or empty.");
        }
        if (checkInAgentRepository.get(agentId) == null) {
            throw new IllegalArgumentException("Check-in agent not found.");
        }
        checkInAgentRepository.updateIdentityDocuments(agentId, documents);
    }

    public void deleteCheckInAgent(String id) {
        validateId(id);
        checkInAgentRepository.delete(id);
    }

    public List<CheckInAgent> getAgentsByAirport(String airportId) {
        if (airportId == null || airportId.isBlank()) {
            throw new IllegalArgumentException("Airport ID cannot be null or blank");
        }
        return checkInAgentRepository.getAgentsByAirport(airportId);
    }

    public void updateCheckInAgentAirport(String agentId, String airportId) {
        validateId(agentId);

        if (airportId == null || airportId.isBlank()) {
            throw new IllegalArgumentException("Airport ID cannot be null or blank.");
        }
        if (checkInAgentRepository.get(agentId) == null) {
            throw new IllegalArgumentException("Check-in agent not found.");
        }
        checkInAgentRepository.updateAssignedAirport(agentId, airportId);
    }
}
