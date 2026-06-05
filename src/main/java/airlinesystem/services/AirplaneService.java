package airlinesystem.services;

import airlinesystem.models.AirplaneStatus;
import airlinesystem.repository.AirplaneRepository;
import airlinesystem.repository.AirplaneModelRepository;
import airlinesystem.models.Airplane;
import java.util.List;

public class AirplaneService {
    private final AirplaneRepository airplaneRepository;
    private final AirplaneModelRepository airplaneModelRepository;

    public AirplaneService(AirplaneRepository airplaneRepository, AirplaneModelRepository airplaneModelRepository) {
        this.airplaneRepository = airplaneRepository;
        this.airplaneModelRepository = airplaneModelRepository;
    }

    private void validateAirplane(Airplane airplane) {
        if (airplane == null || airplane.getAirplaneModel() == null) {
            throw new IllegalArgumentException("Airplane and airplane model cannot be null");
        }

        String modelId = airplane.getAirplaneModel().getId();                                               // must validate that the airplane model exists in the DB
        if (airplaneModelRepository.get(modelId) == null) {
            throw new IllegalArgumentException("The specified airplane model does not exist in the database.");
        }
    }

    public void addAirplane(Airplane airplane) {
        validateAirplane(airplane);
        airplaneRepository.add(airplane);
    }

    public Airplane getAirplane(String id) {
        return airplaneRepository.get(id);
    }

    public List<Airplane> getAllAirplanes() {
        return airplaneRepository.getAll();
    }

    public void updateAirplane(Airplane airplane) {
        validateAirplane(airplane);
        airplaneRepository.update(airplane);
    }

    public void deleteAirplane(String id) {
        airplaneRepository.delete(id);
    }

    // meant to be called as a side effect for other actions
    public void recordFlight(Airplane airplane) {
        airplane.setPressurizationCycles(airplane.getPressurizationCycles() + 1);
        airplane.setLastRevisionCycles(airplane.getLastRevisionCycles() + 1);
        checkRevision(airplane);                                                                        // after updating, must check whether to send the airplane for revision

        updateAirplane(airplane);
    }

    private void checkRevision(Airplane airplane) {
        int modelCycles = airplane.getAirplaneModel().getMaintenanceCycles();                           // maximum allowed pressurization cycles for the airplane model
        int lastRevisionCycles = airplane.getLastRevisionCycles();                                      // number of cycles performed since the last revision

        if (lastRevisionCycles >= modelCycles) {
            performRevision(airplane);
        }
    }

    private void performRevision(Airplane airplane) {
        airplane.setAirplaneStatus(AirplaneStatus.MAINTENANCE);
        airplane.setLastRevisionCycles(0);
    }

    // meant to be performed by the user
    public void releaseFromRevision(String id) {
        Airplane airplane = airplaneRepository.get(id);
        if (airplane == null) {
            throw new IllegalArgumentException("The specified airplane does not exist in the database.");
        }

        if (airplane.getAirplaneStatus() != AirplaneStatus.MAINTENANCE) {
            throw new IllegalStateException("The specified airplane is not currently in maintenance. Its current status is: " + airplane.getAirplaneStatus());
        }
        airplane.setAirplaneStatus(AirplaneStatus.ACTIVE);
        updateAirplane(airplane);
    }

    public void retireAirplane(String id) {
        Airplane airplane = airplaneRepository.get(id);
        if (airplane == null) {
            throw new IllegalArgumentException("The specified airplane does not exist in the database.");
        }

        if (airplane.getAirplaneStatus() == AirplaneStatus.RETIRED) {
            throw new IllegalStateException("The specified airplane is already retired.");
        }
        airplane.setAirplaneStatus(AirplaneStatus.RETIRED);
        updateAirplane(airplane);
    }
}
