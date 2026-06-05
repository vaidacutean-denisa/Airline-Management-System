package airlinesystem.services;

import airlinesystem.models.AirplaneModel;
import airlinesystem.repository.AirplaneModelRepository;
import java.util.List;

public class AirplaneModelService {
    private final AirplaneModelRepository airplaneModelRepository;

    public AirplaneModelService(AirplaneModelRepository airplaneModelRepository) {
        this.airplaneModelRepository = airplaneModelRepository;
    }

    public void addAirplaneModel(AirplaneModel airplaneModel) {
        if (airplaneModel == null) {
            throw new IllegalArgumentException("Airplane model cannot be null");
        }
        airplaneModelRepository.add(airplaneModel);
    }

    public AirplaneModel getAirplaneModel(String id) {
        return airplaneModelRepository.get(id);
    }

    public List<AirplaneModel> getAllAirplaneModels() {
        return airplaneModelRepository.getAll();
    }

    public void updateAirplaneModel(AirplaneModel airplaneModel) {
        if (airplaneModel == null) {
            throw new IllegalArgumentException("Airplane model cannot be null");
        }
        airplaneModelRepository.update(airplaneModel);
    }

    public void deleteAirplaneModel(String id) {
        airplaneModelRepository.delete(id);
    }

    public List<AirplaneModel> getModelsByManufacturer(String manufacturer) {
        if (manufacturer == null || manufacturer.isBlank()) {
            throw new IllegalArgumentException("Manufacturer cannot be null");
        }
        return airplaneModelRepository.getByManufacturer(manufacturer);
    }

    public List<AirplaneModel> getModelsWithMinRange(double minRange) {                             // must assign airplanes that have maxRange >= distance of the route -> returns a list of such models
        if (minRange <= 0) {
            throw new IllegalArgumentException("Minimum range must be positive");
        }
        return airplaneModelRepository.getModelsByRange(minRange);
    }

    public double calculateFuelNeeded(String modelId, double distanceKm) {
        if (distanceKm <= 0) {
            throw new IllegalArgumentException("Distance must be positive");
        }
        AirplaneModel model = airplaneModelRepository.get(modelId);
        if (model == null) {
            throw new IllegalArgumentException("The specified airplane model does not exist in the database.");
        }

        double fuelNeeded = distanceKm * model.getFuelConsumptionPerKm();
        if (fuelNeeded > model.getFuelCapacity()) {
            throw new IllegalArgumentException("The distance exceeds the maximum fuel capacity of this airplane model.");
        }
        return fuelNeeded;
    }
}
