package airlinesystem.services;

import airlinesystem.models.AirplaneModel;
import airlinesystem.models.Flight;
import airlinesystem.models.Pilot;
import airlinesystem.models.IdentityDocument;
import airlinesystem.repository.PilotRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PilotService {
    private final PilotRepository pilotRepository;
    private final AirplaneModelService airplaneModelService;

    public PilotService(PilotRepository pilotRepository, AirplaneModelService airplaneModelService) {
        this.pilotRepository = pilotRepository;
        this.airplaneModelService = airplaneModelService;
    }

    private void validatePilot(Pilot pilot) {
        if (pilot == null) {
            throw new IllegalArgumentException("Pilot cannot be null");
        }

        boolean invalidCertifications = false;
        for (AirplaneModel airplaneModel : pilot.getCertifications().keySet()) {
            if (airplaneModel == null || airplaneModelService.getAirplaneModel(airplaneModel.getId()) == null) {
                invalidCertifications = true;
                break;
            }
        }
        if (invalidCertifications) {
            throw new IllegalArgumentException("Pilot has invalid certifications. (inexistent airplane model)");
        }
    }

    private void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Pilot ID cannot be null or blank.");
        }
    }

    public void addPilot(Pilot pilot) {
        validatePilot(pilot);
        pilotRepository.add(pilot);
    }

    public Pilot getPilot(String id) {
        validateId(id);
        return pilotRepository.get(id);
    }

    public List<Pilot> getAllPilots() {
        return pilotRepository.getAll();
    }

    public void updatePilot(Pilot pilot) {
        validatePilot(pilot);
        pilotRepository.update(pilot);
    }

    public void deletePilot(String id) {
        validateId(id);
        pilotRepository.delete(id);
    }

    public void checkLicense(Pilot pilot, Flight flight) {
        if (pilot == null || flight == null) {
            throw new IllegalArgumentException("Pilot and flight cannot be null.");
        }
        AirplaneModel airplaneModel = flight.getAirplane().getAirplaneModel();

        boolean hasLicense = pilot.getCertifications().containsKey(airplaneModel);
        if (!hasLicense) {
            throw new IllegalArgumentException("Pilot does not have a certification for the airplane model of the flight.");
        }
        checkLicenseValidity(pilot, flight.getFlightDate(), airplaneModel);
    }

    // the certification must be valid (not expired) at the time of the flight
    private void checkLicenseValidity(Pilot pilot, LocalDate flightDate, AirplaneModel airplaneModel) {
        LocalDate expirationDate = pilot.getCertifications().get(airplaneModel);

        // check whether the expiration date is not before the flight date to ensure that even pilots whose license expires in the same day are allowed to fly
        if (expirationDate == null || expirationDate.isBefore(flightDate)) {
            throw new IllegalArgumentException("Pilot's certification for the airplane model has expired or is missing.");
        }
    }

    public void updateCertifications(String pilotId, AirplaneModel airplaneModel, LocalDate expirationDate) {
        validateId(pilotId);
        if (airplaneModel == null || expirationDate == null) {
            throw new IllegalArgumentException("Airplane model and expiration date cannot be null.");
        }
        if (airplaneModelService.getAirplaneModel(airplaneModel.getId()) == null) {
            throw new IllegalArgumentException("The specified airplane model does not exist in the database.");
        }

        Pilot pilot = pilotRepository.get(pilotId);
        if (pilot == null) {
            throw new IllegalArgumentException("The specified pilot does not exist in the database.");
        }
        pilot.addCertification(airplaneModel, expirationDate);
        pilotRepository.update(pilot);
    }

    public List<Pilot> getPilotsForFlight(Flight flight) {
        if (flight == null) {
            throw new IllegalArgumentException("Flight cannot be null.");
        }

        AirplaneModel airplaneModel = flight.getAirplane().getAirplaneModel();
        LocalDate flightDate = flight.getFlightDate();
        List<Pilot> pilots = new ArrayList<>();

        for (Pilot pilot : pilotRepository.getAll()) {
            try {
                checkLicenseValidity(pilot, flightDate, airplaneModel);                         // throws an error if the license is invalid; our goal is to filter pilots, so we ignore it
                pilots.add(pilot);
            } catch (IllegalArgumentException ignored) { }
        }
        return pilots;
    }

    // consider refactoring
    public void updatePilotFirstName(String pilotId, String firstName) {
        validateId(pilotId);
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank.");
        }
        Pilot pilot = pilotRepository.get(pilotId);
        if (pilot == null) {
            throw new IllegalArgumentException("Pilot not found.");
        }
        pilotRepository.updateFirstName(pilotId, firstName);
    }

    public void updatePilotLastName(String pilotId, String lastName) {
        validateId(pilotId);
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank.");
        }
        Pilot pilot = pilotRepository.get(pilotId);
        if (pilot == null) {
            throw new IllegalArgumentException("Pilot not found.");
        }
        pilotRepository.updateLastName(pilotId, lastName);
    }

    public void updatePilotEmail(String pilotId, String email) {
        validateId(pilotId);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank.");
        }
        Pilot pilot = pilotRepository.get(pilotId);
        if (pilot == null) {
            throw new IllegalArgumentException("Pilot not found.");
        }
        pilotRepository.updateEmail(pilotId, email);
    }

    public void updatePilotIdentityDocuments(String pilotId, Set<IdentityDocument> documents) {
        validateId(pilotId);
        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("Identity documents list cannot be null or empty.");
        }
        Pilot pilot = pilotRepository.get(pilotId);
        if (pilot == null) {
            throw new IllegalArgumentException("Pilot not found.");
        }
        pilotRepository.updateIdentityDocuments(pilotId, documents);
    }
}
